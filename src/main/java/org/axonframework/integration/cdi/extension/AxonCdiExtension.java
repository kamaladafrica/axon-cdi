package org.axonframework.integration.cdi.extension;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Sets.filter;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.axonframework.integration.cdi.support.AxonUtils.isAnnotatedAggregateRoot;
import static org.axonframework.integration.cdi.support.AxonUtils.isAnnotatedSaga;
import static org.axonframework.integration.cdi.support.AxonUtils.isCommandHandler;
import static org.axonframework.integration.cdi.support.AxonUtils.isEventHandler;
import static org.axonframework.integration.cdi.support.CdiUtils.normalizedQualifiers;
import static org.axonframework.integration.cdi.support.CdiUtils.typeClosure;

import java.lang.reflect.ParameterizedType;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.AbstractSnapshotter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.integration.cdi.AggregateConfiguration;
import org.axonframework.integration.cdi.AutoConfigure;
import org.axonframework.integration.cdi.SagaConfiguration;
import org.axonframework.integration.cdi.extension.impl.AggregateRootInfo;
import org.axonframework.integration.cdi.extension.impl.AggregateRootInfo.QualifierType;
import org.axonframework.integration.cdi.extension.impl.AutoConfiguringAggregateSnapshotter;
import org.axonframework.integration.cdi.extension.impl.AutoConfiguringCommandBusProducer;
import org.axonframework.integration.cdi.extension.impl.AutoConfiguringEventBusProducer;
import org.axonframework.integration.cdi.extension.impl.AutoConfiguringSagaRepositoryProducer;
import org.axonframework.integration.cdi.extension.impl.AxonConfigInfo;
import org.axonframework.integration.cdi.extension.impl.HandlerInfo;
import org.axonframework.integration.cdi.extension.impl.RepositoryContextualLifecycle;
import org.axonframework.integration.cdi.extension.impl.SagaInfo;
import org.axonframework.integration.cdi.extension.impl.SagaManagerContextualLifecycle;
import org.axonframework.integration.cdi.extension.impl.SagaManagerInfo;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.AnnotatedSagaManager;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class AxonCdiExtension implements Extension {

	private final Set<AxonConfigInfo> configuration = Sets.newHashSet();

	// lookup types
	<X extends EventSourcedAggregateRoot<?>> void processAggregateRootAnnotatedType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isAggregateRoot = isAnnotatedAggregateRoot(at.getJavaClass());
		if (isAggregateRoot) {
			configuration.add(AggregateRootInfo.of(beanManager, at));
			pat.veto();
		} else {
			Preconditions.checkArgument(!at.isAnnotationPresent(AggregateConfiguration.class),
					"Type %s is annotated with @AggregateConfiguration but is not an aggregate root",
					at);
		}

	}

	<X extends AbstractAnnotatedSaga> void processSagaAnnotatedType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (isAnnotatedSaga(at.getJavaClass())) {
			configuration.add(SagaInfo.of(beanManager, at));
			pat.veto();
		} else {
			Preconditions.checkArgument(!at.isAnnotationPresent(SagaConfiguration.class),
					"Type %s is annotated with @SagaConfiguration but is not a saga", at);
		}
	}

	<X> void processCommandsAndEventsHandlerTypes(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isCommandHandler = isCommandHandler(at.getJavaClass());
		boolean isEventHandler = isEventHandler(at.getJavaClass());
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
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> originalProducer = processProducer.getProducer();
			Producer<X> producer = new AutoConfiguringCommandBusProducer<>(originalProducer,
					annotatedMember, getAggregateRoots(), getCommandHandlers(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// event bus
	<T, X extends EventBus> void processEventBusProducer(
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> originalProducer = processProducer.getProducer();
			Producer<X> producer = new AutoConfiguringEventBusProducer<>(originalProducer,
					annotatedMember, getEventHandlers(), getSagas(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// snapshotter
	<T, X extends AbstractSnapshotter> void processSnapshotterProducer(
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> producer = new AutoConfiguringAggregateSnapshotter<>(originalProducer,
					annotatedMember, getAggregateRoots(), beanManager);
			processProducer.setProducer(producer);
		}
	}

	// saga repository
	<T, X extends SagaRepository> void processSagaRepositoryProducer(
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			Producer<X> producer = new AutoConfiguringSagaRepositoryProducer<>(originalProducer,
					annotatedMember, beanManager);
			processProducer.setProducer(producer);
		}
	}

	// add aggregate root repositories and saga managers
	<T extends EventSourcedAggregateRoot<?>> void afterBeanDiscovery(
			@Observes AfterBeanDiscovery afd, BeanManager beanManager) {
		addAggregateRootRepositories(afd, beanManager);
		addSagaManagers(afd, beanManager);
	}

	private <T extends EventSourcedAggregateRoot<?>> void addAggregateRootRepositories(
			AfterBeanDiscovery afd, BeanManager beanManager) {
		for (AggregateRootInfo aggregateRoot : getAggregateRoots()) {
			afd.addBean(createRepositoryBean(beanManager, aggregateRoot));
		}
	}

	private <T extends AbstractAnnotatedSaga> void addSagaManagers(AfterBeanDiscovery afd,
			BeanManager beanManager) {
		Set<SagaManagerInfo> sagaManagers = SagaManagerInfo.from(getSagas());
		for (SagaManagerInfo sagaManager : sagaManagers) {
			afd.addBean(createSagaManagerBean(beanManager, sagaManager));
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends EventSourcedAggregateRoot<?>> Bean<EventSourcingRepository<T>> createRepositoryBean(
			BeanManager bm, AggregateRootInfo aggregateRoot) {

		BeanBuilder<T> aggregateRootBean = new BeanBuilder<T>(bm)
				.readFromType((AnnotatedType<T>) aggregateRoot.getAnnotatedType(bm));

		ParameterizedType type = parameterize(EventSourcingRepository.class,
				aggregateRoot.getType());

		BeanBuilder<EventSourcingRepository<T>> builder = new BeanBuilder<EventSourcingRepository<T>>(
				bm)
						.beanClass(EventSourcingRepository.class)
						.qualifiers(normalizedQualifiers(
								aggregateRoot.getQualifiers(QualifierType.REPOSITORY)))
						.alternative(aggregateRootBean.isAlternative())
						.nullable(aggregateRootBean.isNullable())
						.types(typeClosure(type))
						.scope(aggregateRootBean.getScope())
						.stereotypes(aggregateRootBean.getStereotypes())
						.beanLifecycle(
								new RepositoryContextualLifecycle<T, EventSourcingRepository<T>>(bm,
										aggregateRoot));

		if (!Strings.isNullOrEmpty(aggregateRootBean.getName())) {
			builder.name(aggregateRootBean.getName() + "Repository");
		}
		return builder.create();
	}

	private <T extends AbstractAnnotatedSaga> Bean<AnnotatedSagaManager> createSagaManagerBean(
			BeanManager beanManager, SagaManagerInfo sagaManager) {

		return new BeanBuilder<AnnotatedSagaManager>(beanManager)
				.beanClass(AnnotatedSagaManager.class)
				.qualifiers(normalizedQualifiers(sagaManager.getRepositoryQualifiers()))
				.alternative(false)
				.nullable(false)
				.types(typeClosure(AnnotatedSagaManager.class))
				.beanLifecycle(new SagaManagerContextualLifecycle<T>(beanManager, sagaManager))
				.create();
	}

	public Set<HandlerInfo> getAllHandlers() {
		return filterByType(configuration, HandlerInfo.class);
	}

	public Set<HandlerInfo> getEventHandlers() {
		return filter(getAllHandlers(), EventHandlerPredicate.INSTANCE);
	}

	public Set<HandlerInfo> getCommandHandlers() {
		return filter(getAllHandlers(), not(EventHandlerPredicate.INSTANCE));
	}

	public Set<AggregateRootInfo> getAggregateRoots() {
		return filterByType(configuration, AggregateRootInfo.class);
	}

	public Set<SagaInfo> getSagas() {
		return filterByType(configuration, SagaInfo.class);
	}

	private <T extends AxonConfigInfo> Set<T> filterByType(Iterable<AxonConfigInfo> iterable,
			Class<T> targetClass) {
		Set<AxonConfigInfo> filtered = filter(configuration, Predicates.instanceOf(targetClass));
		Iterable<T> transformed = Iterables.transform(filtered,
				new CastFunction<AxonConfigInfo, T>(targetClass));
		return ImmutableSet.copyOf(transformed);
	}

	private static class EventHandlerPredicate implements Predicate<HandlerInfo> {

		public static final Predicate<HandlerInfo> INSTANCE = new EventHandlerPredicate();

		@Override
		public boolean apply(HandlerInfo input) {
			return input.isEventHandler();
		}

	}

	private static class CastFunction<T, U extends T> implements Function<T, U> {

		private final Class<U> targetClass;

		public CastFunction(Class<U> targetClass) {
			this.targetClass = requireNonNull(targetClass);
		}

		@Override
		public U apply(T input) {
			return targetClass.cast(input);
		}

	}

}
