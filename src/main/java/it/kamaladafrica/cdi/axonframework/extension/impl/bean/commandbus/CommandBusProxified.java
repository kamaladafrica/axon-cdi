package it.kamaladafrica.cdi.axonframework.extension.impl.bean.commandbus;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

public interface CommandBusProxified extends CommandBus {

	default <T> Repository<T> createRepository(AggregateFactory<T> aggregateFactory,
            ParameterResolverFactory parameterResolverFactory) {
		throw new UnsupportedOperationException(); 
	}

	default boolean isDisruptorCommandBus() {
		return DisruptorCommandBusProxified.class.isAssignableFrom(this.getClass());
	}

}
