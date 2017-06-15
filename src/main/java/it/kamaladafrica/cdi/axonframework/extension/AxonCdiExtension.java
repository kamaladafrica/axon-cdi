package it.kamaladafrica.cdi.axonframework.extension;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.saga.SagaRepository;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import it.kamaladafrica.cdi.axonframework.AutoConfigure;
import it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType;
import it.kamaladafrica.cdi.axonframework.extension.impl.AutoConfiguringAggregateSnapshotterProducer;
import it.kamaladafrica.cdi.axonframework.extension.impl.AutoConfiguringCommandBusProducer;
import it.kamaladafrica.cdi.axonframework.extension.impl.AutoConfiguringEventStoreProducer;
import it.kamaladafrica.cdi.axonframework.extension.impl.AutoConfiguringSagaRepositoryProducer;
import it.kamaladafrica.cdi.axonframework.extension.impl.AxonConfigInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.EventSchedulerInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.HandlerInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.RepositoryContextualLifecycle;
import it.kamaladafrica.cdi.axonframework.extension.impl.SagaInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.SimpleEventSchedulerContextualLifecycle;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class AxonCdiExtension implements Extension {

	private static final Logger LOGGER = Logger.getLogger(AxonCdiExtension.class.getName());

	private final Set<AxonConfigInfo> configuration = Sets.newHashSet();

	private final Set<EventSchedulerInfo> eventSchedulerInfos = Sets.newHashSet();

	// lookup types
	<X> void processAggregateRootAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isAggregateRoot = AxonUtils.isAnnotatedAggregateRoot(at.getJavaClass());
		if (isAggregateRoot) {
			configuration.add(AggregateRootInfo.of(beanManager, at));
			pat.veto();
		}

	}

	<X> void processSagaAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (AxonUtils.isAnnotatedSaga(at.getJavaClass())) {
			configuration.add(SagaInfo.of(beanManager, at));
			// pat.veto(); // don't veto this bean. Because we need it to discover EventScheduler injected beans
		}
	}

	<X> void processCommandsAndEventsHandlerTypes(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isCommandHandler = AxonUtils.isCommandHandler(at.getJavaClass());
		boolean isEventHandler = AxonUtils.isEventHandler(at.getJavaClass());
		Preconditions.checkArgument(!isEventHandler || !isCommandHandler,
				"Provided type cannot be both event and command handler: %s", at);
		if (isCommandHandler) {
			configuration.add(HandlerInfo.commandHandler(beanManager, at));
		} else if (isEventHandler) {
			configuration.add(HandlerInfo.eventHandler(beanManager, at));
		}
	}

	// command bus
	<T, X extends CommandBus> void processCommandBusProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {

		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> originalProducer = processProducer.getProducer();
			Producer<X> producer = new AutoConfiguringCommandBusProducer<>(originalProducer,
					annotatedMember, getAggregateRoots(), getCommandHandlers(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// event bus
	// nothing to do...
	// event store is an event bus

	// event store
	<T, X extends EventStore> void processEventStoreProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();
		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> originalProducer = processProducer.getProducer();
			Producer<X> producer = new AutoConfiguringEventStoreProducer<>(originalProducer,
					annotatedMember,
					getEventHandlers(),
					getSagas(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// snapshotter
	<T, X extends Snapshotter> void processSnapshotterProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {
		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();
		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> producer = new AutoConfiguringAggregateSnapshotterProducer<>(originalProducer,
					annotatedMember, getAggregateRoots(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// saga repository
	<T, X extends SagaRepository<?>> void processSagaRepositoryProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {
		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();
		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> producer = new AutoConfiguringSagaRepositoryProducer<>(originalProducer,
					annotatedMember, beanManager);
			processProducer.setProducer(producer);
		}
	}

	// EventScheduler
	<T, X extends EventScheduler> void processEventSchedulerInjectionPoint(
			@Observes final ProcessInjectionPoint<T, X> processInjectionPoint,
			final BeanManager beanManager) {
		InjectionPoint injectionPoint = processInjectionPoint.getInjectionPoint();
		eventSchedulerInfos.add(EventSchedulerInfo.of(injectionPoint));
	}

	// add new bean after discovering depending beans
	<T> void afterBeanDiscovery(
			@Observes final AfterBeanDiscovery afd, final BeanManager beanManager) {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Activated");
		addAggregateRootRepositories(afd, beanManager);
		addEventScheduler(afd, beanManager);
	}

	protected void afterDeploymentValidation(@Observes final AfterDeploymentValidation adv, final BeanManager bm) {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Init bean references");
		// Snapshotter
		Set<Bean<?>> snapshotterbeans = bm.getBeans(Snapshotter.class, new AnyLiteral());
		for (Bean<?> bean: snapshotterbeans) {
			Snapshotter snapshotter = (Snapshotter) bm.getReference(bean, Snapshotter.class, bm.createCreationalContext(null));
			LOGGER.log(Level.FINE, "Axon CDI Extension - Init bean reference " + snapshotter);
		}
		// EventStore
		// Remember no SimpleEventBus because in axon 3.x EventStore now extends EventBus
		Set<Bean<?>> eventStorebeans = bm.getBeans(EventBus.class, new AnyLiteral());
		for (Bean<?> bean: eventStorebeans) {
			EventBus eventStore = (EventBus) bm.getReference(bean, EventBus.class, bm.createCreationalContext(null));
			LOGGER.log(Level.FINE, "Axon CDI Extension - Init bean reference " + eventStore);
		}
		// CommandBus
		Set<Bean<?>> commandBusbeans = bm.getBeans(CommandBus.class, new AnyLiteral());
		for (Bean<?> bean: commandBusbeans) {
			CommandBus commandBus = (CommandBus) bm.getReference(bean, CommandBus.class, bm.createCreationalContext(null));
			LOGGER.log(Level.FINE, "Axon CDI Extension - Init bean reference " + commandBus);
		}
	}

	private <T> void addAggregateRootRepositories(final AfterBeanDiscovery afd, final BeanManager beanManager) {
		for (AggregateRootInfo aggregateRootInfo : getAggregateRoots()) {
			afd.addBean(createRepositoryBean(beanManager, aggregateRootInfo));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Bean<EventSourcingRepository<T>> createRepositoryBean(
			final BeanManager bm, final AggregateRootInfo aggregateRootInfo) {

		BeanBuilder<T> aggregateRootBean = new BeanBuilder<T>(bm)
				.readFromType((AnnotatedType<T>) aggregateRootInfo.getAnnotatedType(bm));

		ParameterizedType type = TypeUtils.parameterize(EventSourcingRepository.class,
				aggregateRootInfo.getType());

		BeanBuilder<EventSourcingRepository<T>> builder = new BeanBuilder<EventSourcingRepository<T>>(
				bm)
						.beanClass(EventSourcingRepository.class)
						.qualifiers(CdiUtils.normalizedQualifiers(
								aggregateRootInfo.getQualifiers(QualifierType.REPOSITORY)))
						.alternative(aggregateRootBean.isAlternative())
						.nullable(aggregateRootBean.isNullable())
						.types(CdiUtils.typeClosure(type))
						.scope(aggregateRootBean.getScope())
						.stereotypes(aggregateRootBean.getStereotypes())
						.beanLifecycle(
								new RepositoryContextualLifecycle<T, EventSourcingRepository<T>>(bm,
										aggregateRootInfo));

		if (!Strings.isNullOrEmpty(aggregateRootBean.getName())) {
			builder.name(aggregateRootBean.getName() + "Repository");
		}
		return builder.create();
	}

	private void addEventScheduler(final AfterBeanDiscovery afd, final BeanManager beanManager) {
		for (EventSchedulerInfo eventSchedulerInfo : eventSchedulerInfos) {
			afd.addBean(createEventSchedulerBean(beanManager, eventSchedulerInfo));
		}
	}

	private Bean<EventScheduler> createEventSchedulerBean(final BeanManager bm,
			final EventSchedulerInfo eventSchedulerInfo) {
		BeanBuilder<EventScheduler> builder = new BeanBuilder<EventScheduler>(bm)
				.beanClass(EventScheduler.class)
				.qualifiers(CdiUtils.normalizedQualifiers(eventSchedulerInfo.getQualifiers()))
				.types(EventScheduler.class)
				.beanLifecycle(
						new SimpleEventSchedulerContextualLifecycle<EventScheduler>(bm, eventSchedulerInfo));
		return builder.create();
	}

	public Set<HandlerInfo> getAllHandlers() {
		return filterByType(configuration, HandlerInfo.class);
	}

	public Set<HandlerInfo> getEventHandlers() {
		return Sets.filter(getAllHandlers(), EventHandlerPredicate.INSTANCE);
	}

	public Set<HandlerInfo> getCommandHandlers() {
		return Sets.filter(getAllHandlers(), Predicates.not(EventHandlerPredicate.INSTANCE));
	}

	public Set<AggregateRootInfo> getAggregateRoots() {
		return filterByType(configuration, AggregateRootInfo.class);
	}

	public Set<SagaInfo> getSagas() {
		return filterByType(configuration, SagaInfo.class);
	}

	private <T extends AxonConfigInfo> Set<T> filterByType(final Iterable<AxonConfigInfo> iterable,
			final Class<T> targetClass) {
		Set<AxonConfigInfo> filtered = Sets.filter(configuration, Predicates.instanceOf(targetClass));
		Iterable<T> transformed = Iterables.transform(filtered,
				new CastFunction<AxonConfigInfo, T>(targetClass));
		return ImmutableSet.copyOf(transformed);
	}

	private static class EventHandlerPredicate implements Predicate<HandlerInfo> {

		public static final Predicate<HandlerInfo> INSTANCE = new EventHandlerPredicate();

		@Override
		public boolean apply(final HandlerInfo input) {
			return input.isEventHandler();
		}

	}

	private static class CastFunction<T, U extends T> implements Function<T, U> {

		private final Class<U> targetClass;

		public CastFunction(final Class<U> targetClass) {
			this.targetClass = Objects.requireNonNull(targetClass);
		}

		@Override
		public U apply(final T input) {
			return targetClass.cast(input);
		}

	}

}
