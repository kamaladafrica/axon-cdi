package org.axonframework.integration.cdi.extension.impl;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.TypeLiteral;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.integration.cdi.support.CdiUtils;

public class AutoConfiguringCommandBusProducer<X extends CommandBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> handlers;

	public AutoConfiguringCommandBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<HandlerInfo> handlers,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlers = handlers;
	}

	@Override
	protected X configure(X commandBus) {
		registerCommandHandlers(commandBus);
		registerRepositories(commandBus);
		return commandBus;
	}

	private void registerCommandHandlers(X commandBus) {
		for (HandlerInfo handler : handlers) {
			AnnotationCommandHandlerAdapter.subscribe(handler.getReference(getBeanManager()),
					commandBus);
		}
	}

	@SuppressWarnings({ "unchecked", "serial" })
	protected void registerRepositories(X commandBus) {
		TypeLiteral<EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>> repositoryTypeLiteral = new TypeLiteral<EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>>() {};
		Type repositoryType = repositoryTypeLiteral.getType();
		Set<Bean<?>> repositoryBeans = getBeanManager().getBeans(repositoryType, getQualifiers());
		for (Bean<?> bean : repositoryBeans) {
			EventSourcingRepository<? extends EventSourcedAggregateRoot<?>> repository = (EventSourcingRepository<? extends EventSourcedAggregateRoot<?>>) CdiUtils
					.getReference(getBeanManager(), bean, repositoryType);
			subscribe(repository, commandBus);
		}
	}

	private <T extends EventSourcedAggregateRoot<?>> void subscribe(
			EventSourcingRepository<T> repository, X commandBus) {
		AggregateAnnotationCommandHandler.subscribe(repository.getAggregateFactory()
				.getAggregateType(), repository, commandBus);
	}

}
