package org.axonframework.integration.cdi.extension;

import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.axonframework.integration.cdi.AxonUtils.asTypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.AggregateFactory;
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
		
		R repository;
		AggregateFactory<T> aggregateFactory = selectAggregateFactory(qualifiers);
		if(aggregateFactory == null){
			repository = (R) new EventSourcingRepository<T>(aggregateRootClass, eventStore);			
		} else {
			repository = (R) new EventSourcingRepository<T>(aggregateFactory, eventStore);
		}

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

	private AggregateFactory<T> selectAggregateFactory(Annotation... qualifiers) {
		TypeLiteral<AggregateFactory<T>> type = asTypeLiteral(
				parameterize(AggregateFactory.class, aggregateRootClass));
		Instance<AggregateFactory<T>> instances = CDI.current().select(type,
				qualifiers);
		return instances.isUnsatisfied() ? null : instances.get();
	}

	@Override
	public void destroy(Bean<R> bean, R instance, CreationalContext<R> creationalContext) {
		creationalContext.release();
	}

}
