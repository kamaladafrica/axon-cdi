package org.axonframework.integration.cdi.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.TypeLiteral;

import org.axonframework.eventsourcing.AbstractSnapshotter;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventstore.SnapshotEventStore;
import org.axonframework.unitofwork.TransactionManager;

public class AutoConfiguringAggregateSnapshotter<X extends AbstractSnapshotter> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<AnnotatedType<? extends EventSourcedAggregateRoot<?>>> aggregateRootTypes;

	public AutoConfiguringAggregateSnapshotter(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<AnnotatedType<? extends EventSourcedAggregateRoot<?>>> aggregateRootTypes,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.aggregateRootTypes = aggregateRootTypes;
	}

	@Override
	protected X configure(X snapshotter) {
		Instance<SnapshotEventStore> eventStores = CDI.current().select(SnapshotEventStore.class);
		if (eventStores.isAmbiguous()) {
			eventStores = eventStores.select(getQualifiers());
		}
		snapshotter.setEventStore(eventStores.get());

		Instance<Executor> executors = CDI.current().select(Executor.class);
		if (executors.isAmbiguous()) {
			executors = executors.select(getQualifiers());
		}
		if (!executors.isUnsatisfied()) {
			snapshotter.setExecutor(executors.get());
		}

		@SuppressWarnings("serial")
		Instance<TransactionManager<?>> transactionManagers = CDI.current().select(
				new TypeLiteral<TransactionManager<?>>() {});
		if (transactionManagers.isAmbiguous()) {
			transactionManagers = transactionManagers.select(getQualifiers());
		}
		if (!transactionManagers.isUnsatisfied()) {
			snapshotter.setTxManager(transactionManagers.get());
		}

		if (snapshotter instanceof AggregateSnapshotter) {
			AggregateSnapshotter aggregateSnapshotter = (AggregateSnapshotter) snapshotter;
			registerAggregateFactories(aggregateSnapshotter);
		}
		return snapshotter;
	}

	@SuppressWarnings("unchecked")
	private <T extends EventSourcedAggregateRoot<?>> void registerAggregateFactories(
			AggregateSnapshotter snapshotter) {
		List<AggregateFactory<?>> factories = new ArrayList<AggregateFactory<?>>();
		for (AnnotatedType<? extends EventSourcedAggregateRoot<?>> type : aggregateRootTypes) {
			factories.add(new GenericAggregateFactory<T>((Class<T>) type.getJavaClass()));
		}
		snapshotter.setAggregateFactories(factories);
	}
}
