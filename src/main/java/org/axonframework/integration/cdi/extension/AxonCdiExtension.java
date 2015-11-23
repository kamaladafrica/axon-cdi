package org.axonframework.integration.cdi.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.BeanUtils;
import org.apache.deltaspike.core.util.HierarchyDiscovery;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.AbstractSnapshotter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.integration.cdi.AutoConfigure;
import org.axonframework.integration.cdi.AxonCdiHelper;
import org.axonframework.saga.SagaManager;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.AnnotatedSagaManager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import static org.apache.deltaspike.core.util.BeanUtils.getQualifiers;
import static org.axonframework.integration.cdi.AxonUtils.isAnnotatedAggregateRoot;
import static org.axonframework.integration.cdi.AxonUtils.isAnnotatedSaga;
import static org.axonframework.integration.cdi.AxonUtils.isCommandHandler;
import static org.axonframework.integration.cdi.AxonUtils.isEventHandler;

public class AxonCdiExtension implements Extension {

	private final Set<AnnotatedType<?>> commandHandlerTypes = new HashSet<>();

	private final Set<AnnotatedType<?>> eventHandlerTypes = new HashSet<>();

	private final Set<AnnotatedType<? extends EventSourcedAggregateRoot<?>>> annotatedAggregateRootTypes = new HashSet<>();

	private final Set<AnnotatedType<? extends AbstractAnnotatedSaga>> annotatedSagaTypes = new HashSet<>();

	// lookup types
	<X extends EventSourcedAggregateRoot<?>> void processAggregateRootAnnotatedType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (isAnnotatedAggregateRoot(at.getJavaClass())) {
			annotatedAggregateRootTypes.add(at);
			pat.veto();
		}
	}

	<X extends AbstractAnnotatedSaga> void processSagaAnnotatedType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (isAnnotatedSaga(at.getJavaClass())) {
			annotatedSagaTypes.add(at);
			pat.veto();
		}
	}

	<X> void processCommandHandlersType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (isCommandHandler(at.getJavaClass())) {
			commandHandlerTypes.add(at);
		}
	}

	<X> void processEventHandlersType(
			@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (isEventHandler(at.getJavaClass())) {
			eventHandlerTypes.add(at);
		}
	}

	// command bus
	<T, X extends CommandBus> void processCommandBusProducer(
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			List<Class<?>> commandHandlers = commandHandlersFor(annotatedMember, beanManager);
			Producer<X> producer = new AutoConfiguringCommandBusProducer<>(originalProducer,
					annotatedMember, commandHandlers, beanManager);
			processProducer.setProducer(producer);
		}
	}

	// event bus
	<T, X extends EventBus> void processEventBusProducer(
			@Observes ProcessProducer<T, X> processProducer,
			BeanManager beanManager) {

		Producer<X> originalProducer = processProducer.getProducer();
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();

		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			List<Class<?>> eventHandlers = eventHandlersFor(annotatedMember, beanManager);
			Producer<X> producer = new AutoConfiguringEventBusProducer<>(originalProducer,
					annotatedMember, eventHandlers, beanManager);
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
					annotatedMember, annotatedAggregateRootTypes, beanManager);
			processProducer.setProducer(producer);
		}
	}

	// event bus
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

	private List<Class<?>> commandHandlersFor(Annotated annotated, BeanManager beanManager) {
		List<Class<?>> handlers = new ArrayList<>();
		for (AnnotatedType<?> at : commandHandlerTypes) {
			handlers.add(at.getJavaClass());
		}
		return handlers;
	}

	private List<Class<?>> eventHandlersFor(Annotated annotated, BeanManager beanManager) {
		List<Class<?>> handlers = new ArrayList<>();
		Set<Annotation> qualifiers = BeanUtils.getQualifiers(beanManager,
				annotated.getAnnotations());
		for (AnnotatedType<?> at : eventHandlerTypes) {
			if (Objects.equals(qualifiers, getQualifiers(beanManager, at.getAnnotations()))) {
				handlers.add(at.getJavaClass());
			}
		}
		return handlers;
	}

	<T extends EventSourcedAggregateRoot<?>> void afterBeanDiscovery(
			@Observes AfterBeanDiscovery afd, BeanManager beanManager) {
		addAggregateRootRepositories(afd, beanManager);
		addSagaManagers(afd, beanManager);
		addHelper(afd, beanManager);
	}

	private void addHelper(AfterBeanDiscovery afd, BeanManager beanManager) {
		BeanBuilder<AxonCdiHelper> builder = new BeanBuilder<AxonCdiHelper>(
				beanManager)
				.beanClass(AxonCdiHelper.class)
				.types(AxonCdiHelper.class)
				.beanLifecycle(new ContextualLifecycle<AxonCdiHelper>() {

					@Override
					public AxonCdiHelper create(Bean<AxonCdiHelper> bean,
							CreationalContext<AxonCdiHelper> creationalContext) {
						return new InjectableAxonCdiHelper(AxonCdiExtension.this);
					}

					@Override
					public void destroy(Bean<AxonCdiHelper> bean, AxonCdiHelper instance,
							CreationalContext<AxonCdiHelper> creationalContext) {}

				});
		afd.addBean(builder.create());
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractAnnotatedSaga> void addSagaManagers(AfterBeanDiscovery afd,
			BeanManager beanManager) {
		MultiMap map = new MultiValueMap();
		for (AnnotatedType<? extends AbstractAnnotatedSaga> at : annotatedSagaTypes) {
			AnnotatedType<T> sagaType = (AnnotatedType<T>) at;
			map.put(getQualifiers(beanManager, sagaType.getAnnotations()), at);
		}
		for (Object item : map.entrySet()) {
			Entry<Set<Annotation>, Collection<AnnotatedType<T>>> entry = (Entry<Set<Annotation>, Collection<AnnotatedType<T>>>) item;
			Bean<AnnotatedSagaManager> bean = createSagaManagerBean(beanManager, entry.getKey(),
					entry.getValue());
			afd.addBean(bean);
		}
	}

	private <T extends AbstractAnnotatedSaga> Bean<AnnotatedSagaManager> createSagaManagerBean(
			BeanManager beanManager, Set<Annotation> qualifiers,
			Collection<AnnotatedType<T>> sagaTypes) {

		ContextualLifecycle<AnnotatedSagaManager> lifecycle = new SagaManagerContextualLifecycle<T>(
				sagaTypes);

		BeanBuilder<AnnotatedSagaManager> builder = new BeanBuilder<AnnotatedSagaManager>(
				beanManager)
				.beanClass(AnnotatedSagaManager.class)
				.qualifiers(fixQualifiers(qualifiers))
				.alternative(false)
				.nullable(false)
				.types(SagaManager.class, AnnotatedSagaManager.class)
				.beanLifecycle(lifecycle);

		return builder.create();
	}

	private static Set<Annotation> fixQualifiers(Set<Annotation> candidates) {
		Set<Annotation> qualifiers = new HashSet<>(candidates);
		if (qualifiers.isEmpty()) {
			qualifiers.add(new DefaultLiteral());
		}
		qualifiers.add(new AnyLiteral());
		return qualifiers;
	}

	private <T extends EventSourcedAggregateRoot<?>> void addAggregateRootRepositories(
			AfterBeanDiscovery afd, BeanManager beanManager) {
		for (AnnotatedType<? extends EventSourcedAggregateRoot<?>> at : annotatedAggregateRootTypes) {
			@SuppressWarnings("unchecked")
			AnnotatedType<T> aggregateRootType = (AnnotatedType<T>) at;
			Bean<EventSourcingRepository<T>> bean = createRepositoryBean(aggregateRootType,
					beanManager, getQualifiers(beanManager, aggregateRootType.getAnnotations()));
			afd.addBean(bean);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends EventSourcedAggregateRoot<?>> Bean<EventSourcingRepository<T>> createRepositoryBean(
			AnnotatedType<T> aggregateRootType, BeanManager beanManager, Set<Annotation> qualifiers) {

		BeanBuilder<T> aggregateRootBean = new BeanBuilder<T>(beanManager)
				.readFromType(aggregateRootType);

		ContextualLifecycle<EventSourcingRepository<T>> lifecycle = new RepositoryContextualLifecycle<>(
				(Class<T>) aggregateRootBean.getBeanClass());

		ParameterizedType type = TypeUtils.parameterize(EventSourcingRepository.class,
				aggregateRootType.getJavaClass());

		BeanBuilder<EventSourcingRepository<T>> builder = new BeanBuilder<EventSourcingRepository<T>>(
				beanManager)
				.beanClass(EventSourcingRepository.class)
				.qualifiers(aggregateRootBean.getQualifiers())
				.alternative(aggregateRootBean.isAlternative())
				.nullable(aggregateRootBean.isNullable())
				.types(new HierarchyDiscovery(type).getTypeClosure())
				.scope(aggregateRootBean.getScope())
				.stereotypes(aggregateRootBean.getStereotypes())
				.beanLifecycle(lifecycle);
		if (StringUtils.isNotBlank(aggregateRootBean.getName())) {
			builder.name(aggregateRootBean.getName() + "Repository");
		}
		return builder.create();
	}

	public Set<Class<?>> getCommandHandlerClasses() {
		return ImmutableSet.copyOf(Iterables.transform(commandHandlerTypes,
				ToClassFunction.INSTANCE));
	}

	public Set<Class<?>> getEventHandlerClasses() {
		return ImmutableSet
				.copyOf(Iterables.transform(eventHandlerTypes, ToClassFunction.INSTANCE));
	}

	public Set<Class<? extends EventSourcedAggregateRoot<?>>> getAnnotatedAggregateRootClasses() {
		return ImmutableSet.copyOf(Iterables.transform(annotatedAggregateRootTypes,
				new ToClassFunction<EventSourcedAggregateRoot<?>>()));
	}

	public Set<Class<? extends AbstractAnnotatedSaga>> getAnnotatedSagaClasses() {
		return ImmutableSet.copyOf(Iterables.transform(annotatedSagaTypes,
				new ToClassFunction<AbstractAnnotatedSaga>()));
	}

	public static class ToClassFunction<T> implements
			Function<AnnotatedType<?>, Class<? extends T>> {

		public static final Function<AnnotatedType<?>, Class<?>> INSTANCE = new ToClassFunction<>();

		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends T> apply(AnnotatedType<?> input) {
			return (Class<? extends T>) input.getClass();
		}

	}

}
