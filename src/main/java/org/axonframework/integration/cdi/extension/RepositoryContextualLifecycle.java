package org.axonframework.integration.cdi.extension;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.ConflictResolver;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.SnapshotterTrigger;
import org.axonframework.eventstore.EventStore;

public class RepositoryContextualLifecycle<T extends EventSourcedAggregateRoot<?>, R extends EventSourcingRepository<T>>
		implements ContextualLifecycle<R> {

	private final Class<T> aggregateRootClass;

	public RepositoryContextualLifecycle(Class<T> aggregateRootClass) {
		this.aggregateRootClass = aggregateRootClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public R create(Bean<R> bean, CreationalContext<R> creationalContext) {
		Annotation[] qualifiers = bean.getQualifiers().toArray(
				new Annotation[bean.getQualifiers().size()]);
		Instance<Object> instances = CDI.current();
		EventStore eventStore = instances.select(EventStore.class, qualifiers).get();
		R repository = (R) new EventSourcingRepository<T>(aggregateRootClass, eventStore);

		Instance<EventBus> eventBus = instances.select(EventBus.class, qualifiers);
		if (!eventBus.isUnsatisfied()) {
			repository.setEventBus(eventBus.get());
		}

		Instance<ConflictResolver> conflictResolver = instances.select(ConflictResolver.class,
				qualifiers);
		if (!conflictResolver.isUnsatisfied()) {
			repository.setConflictResolver(conflictResolver.get());
		}

		Instance<SnapshotterTrigger> trigger = instances.select(SnapshotterTrigger.class,
				qualifiers);
		if (!trigger.isUnsatisfied()) {
			repository.setSnapshotterTrigger(trigger.get());
		}

		return repository;
	}

	@Override
	public void destroy(Bean<R> bean, R instance, CreationalContext<R> creationalContext) {
		creationalContext.release();
	}

}
