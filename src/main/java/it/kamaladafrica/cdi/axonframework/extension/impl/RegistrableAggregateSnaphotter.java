package it.kamaladafrica.cdi.axonframework.extension.impl;

import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.google.common.collect.Lists;

public class RegistrableAggregateSnaphotter extends AggregateSnapshotter {

	public RegistrableAggregateSnaphotter(EventStore eventStore) {
		super(eventStore, Lists.<AggregateFactory<?>> newArrayList());
	}

	protected void registerAggregateFactory(AggregateFactory<?> aggregateFactory) {
		super.registerAggregateFactory(aggregateFactory);
	}

}
