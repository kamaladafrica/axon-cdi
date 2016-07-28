package org.axonframework.integration.cdi.extension.impl;

import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.axonframework.integration.cdi.extension.impl.AggregateRootInfo.QualifierType.CONFLICT_RESOLVER;
import static org.axonframework.integration.cdi.extension.impl.AggregateRootInfo.QualifierType.EVENT_BUS;
import static org.axonframework.integration.cdi.extension.impl.AggregateRootInfo.QualifierType.REPOSITORY;
import static org.axonframework.integration.cdi.extension.impl.AggregateRootInfo.QualifierType.SNAPSHOTTER_TRIGGER;
import static org.axonframework.integration.cdi.support.AxonUtils.asTypeLiteral;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.ConflictResolver;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.SnapshotterTrigger;
import org.axonframework.eventstore.EventStore;
import org.axonframework.integration.cdi.support.CdiAggregateFactory;
import org.axonframework.integration.cdi.support.CdiParameterResolverFactory;
import org.axonframework.integration.cdi.support.CdiUtils;

public class RepositoryContextualLifecycle<T extends EventSourcedAggregateRoot<?>, R extends EventSourcingRepository<T>>
		implements ContextualLifecycle<R> {

	private final AggregateRootInfo aggregateRoot;

	private final BeanManager beanManager;

	public RepositoryContextualLifecycle(BeanManager beanManager, AggregateRootInfo aggregateRoot) {
		this.aggregateRoot = Objects.requireNonNull(aggregateRoot);
		this.beanManager = Objects.requireNonNull(beanManager);
	}

	@Override
	@SuppressWarnings("unchecked")
	public R create(Bean<R> bean, CreationalContext<R> creationalContext) {
		R repository = (R) new EventSourcingRepository<T>(aggregateFactory(), eventStore());
		repository.setEventBus(eventBus());

		ConflictResolver cr = conflictResolver();
		if (cr != null) {
			repository.setConflictResolver(cr);
		}

		SnapshotterTrigger st = snapshotterTrigger();
		if (st != null) {
			repository.setSnapshotterTrigger(st);
		}

		return repository;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private AggregateFactory<T> aggregateFactory() {
		TypeLiteral<AggregateFactory<T>> typeLiteral = asTypeLiteral(
				parameterize(AggregateFactory.class, aggregateRoot.getType()));
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, typeLiteral.getType(),
				aggregateRoot.getQualifiers(REPOSITORY));
		Bean<?> bean = beanManager.resolve(beans);
		if (bean == null) {
			return (AggregateFactory<T>) new CdiAggregateFactory(beanManager,
					aggregateRoot.getType(), new CdiParameterResolverFactory(beanManager));
		}
		return (AggregateFactory<T>) CdiUtils.getReference(beanManager, bean, typeLiteral.getType());
	}

	private EventStore eventStore() {
		return (EventStore) CdiUtils.getReference(beanManager, EventStore.class,
				aggregateRoot.getQualifiers(REPOSITORY));
	}

	private EventBus eventBus() {
		return (EventBus) CdiUtils.getReference(beanManager, EventBus.class,
				aggregateRoot.getQualifiers(EVENT_BUS));
	}

	private ConflictResolver conflictResolver() {
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, ConflictResolver.class,
				aggregateRoot.getQualifiers(CONFLICT_RESOLVER));
		Bean<?> bean = beanManager.resolve(beans);
		return (ConflictResolver) (bean == null ? null : CdiUtils.getReference(beanManager, bean, ConflictResolver.class));
	}

	private SnapshotterTrigger snapshotterTrigger() {
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, SnapshotterTrigger.class,
				aggregateRoot.getQualifiers(SNAPSHOTTER_TRIGGER));
		Bean<?> bean = beanManager.resolve(beans);
		return (SnapshotterTrigger) (bean == null ? null
				: CdiUtils.getReference(beanManager, bean, SnapshotterTrigger.class));
	}

	@Override
	public void destroy(Bean<R> bean, R instance, CreationalContext<R> creationalContext) {
		creationalContext.release();
	}

}
