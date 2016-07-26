package org.axonframework.integration.cdi.extension;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;

public class AutoConfiguringCommandBusProducer<X extends CommandBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final List<Class<?>> handlerClasses;

	public AutoConfiguringCommandBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			List<Class<?>> handlerClasses,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlerClasses = handlerClasses;
	}

	@Override
	protected X configure(X commandBus) {
		registerCommandHandlers(commandBus);
		registerRepositories(commandBus);
		return commandBus;
	}

	private void registerCommandHandlers(X commandBus) {
		for (Class<?> handlerClass : handlerClasses) {
			Instance<?> handlerInstances = CDI.current().select(handlerClass, getQualifiers());
			if (handlerInstances.isUnsatisfied()) {
				handlerInstances = CDI.current().select(handlerClass);
			}
			Object handler = handlerInstances.get();
			AnnotationCommandHandlerAdapter.subscribe(handler, commandBus);
		}
	}

	protected void registerRepositories(X commandBus) {

		@SuppressWarnings("serial")
		TypeLiteral<EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>> repositoryType = new TypeLiteral<EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>>() {};
		Instance<EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>> repositoryInstances = CDI
				.current().select(
						repositoryType, getQualifiers());
		if (repositoryInstances.isUnsatisfied()) {
			repositoryInstances = CDI.current().select(repositoryType, new AnyLiteral());
		}
		for (EventSourcingRepository<? extends EventSourcedAggregateRoot<?>> repository : repositoryInstances) {
			subscribe(repository, commandBus);

		}
	}

	private <T extends EventSourcedAggregateRoot<?>> void subscribe(
			EventSourcingRepository<T> repository, X commandBus) {
		AggregateAnnotationCommandHandler.subscribe(repository.getAggregateFactory()
				.getAggregateType(), repository, commandBus);
	}

}
