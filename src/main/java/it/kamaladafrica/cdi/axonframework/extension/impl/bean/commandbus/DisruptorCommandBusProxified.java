package it.kamaladafrica.cdi.axonframework.extension.impl.bean.commandbus;

import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.commandhandling.disruptor.DisruptorConfiguration;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

public class DisruptorCommandBusProxified extends DisruptorCommandBus implements CommandBusProxified {

	public DisruptorCommandBusProxified(EventStore eventStore) {
		super(eventStore);
	}

	public DisruptorCommandBusProxified(EventStore eventStore, DisruptorConfiguration configuration) {
		super(eventStore, configuration);
	}

	@Override
	public <T> Repository<T> createRepository(AggregateFactory<T> aggregateFactory,
            ParameterResolverFactory parameterResolverFactory) {
		return super.createRepository(aggregateFactory, parameterResolverFactory);
	}

}
