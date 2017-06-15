package it.kamaladafrica.cdi.axonframework.extension.impl;

import static it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType.REPOSITORY;
import static it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION;
import static it.kamaladafrica.cdi.axonframework.support.AxonUtils.asTypeLiteral;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStore;

//Cf AggregateConfigurer
public class RepositoryContextualLifecycle<T, R extends EventSourcingRepository<T>>
		implements ContextualLifecycle<R> {

	private final AggregateRootInfo aggregateRoot;

	private final BeanManager beanManager;

	public RepositoryContextualLifecycle(final BeanManager beanManager, final AggregateRootInfo aggregateRoot) {
		this.beanManager = Objects.requireNonNull(beanManager);
		this.aggregateRoot = Objects.requireNonNull(aggregateRoot);
	}

	@Override
	@SuppressWarnings("unchecked")
	public R create(final Bean<R> bean, final CreationalContext<R> creationalContext) {
		R repository = (R) new EventSourcingRepository<T>(aggregateFactory(),
				eventStore(),
				snapshotTriggerDefinition());
		return repository;
	}

	@SuppressWarnings({ "unchecked" })
	private AggregateFactory<T> aggregateFactory() {
		TypeLiteral<AggregateFactory<T>> typeLiteral = asTypeLiteral(
				parameterize(AggregateFactory.class, aggregateRoot.getType()));
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, typeLiteral.getType(),
				aggregateRoot.getQualifiers(REPOSITORY));
		Bean<?> bean = beanManager.resolve(beans);
		if (bean == null) {
			return (AggregateFactory<T>) new GenericAggregateFactory<>(aggregateRoot.getType());
		}
		return (AggregateFactory<T>) CdiUtils.getReference(beanManager, bean, typeLiteral.getType());
	}

	private EventStore eventStore() {
		return (EventStore) CdiUtils.getReference(beanManager, EventStore.class,
				aggregateRoot.getQualifiers(REPOSITORY));
	}

	private SnapshotTriggerDefinition snapshotTriggerDefinition() {
		// TODO si le bean est null je dois créer une version par defaut !!!
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, SnapshotTriggerDefinition.class,
				aggregateRoot.getQualifiers(SNAPSHOTTER_TRIGGER_DEFINITION));
		Bean<?> bean = beanManager.resolve(beans);
		return (SnapshotTriggerDefinition) (bean == null ? null
				: CdiUtils.getReference(beanManager, bean, SnapshotTriggerDefinition.class));
	}

	@Override
	public void destroy(final Bean<R> bean, final R instance, final CreationalContext<R> creationalContext) {
		creationalContext.release();
	}

}
