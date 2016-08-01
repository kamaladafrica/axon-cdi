package it.kamaladafrica.cdi.axonframework.extension.impl;

import static com.google.common.collect.ImmutableSet.copyOf;
import static it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType.COMMAND_BUS;
import static it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType.REPOSITORY;
import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.getReference;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler.subscribe;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;

public class AutoConfiguringCommandBusProducer<X extends CommandBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> handlers;

	private final Set<AggregateRootInfo> aggregateRoots;

	public AutoConfiguringCommandBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<AggregateRootInfo> aggregateRoots,
			Set<HandlerInfo> handlers,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlers = handlers;
		this.aggregateRoots = aggregateRoots;
	}

	@Override
	protected X configure(X commandBus) {
		registerCommandHandlers(commandBus);
		registerRepositories(commandBus);
		return commandBus;
	}

	private void registerCommandHandlers(X commandBus) {
		for (HandlerInfo handler : handlers) {
			Set<Bean<?>> beans = getBeanManager().getBeans(handler.getType(), getQualifiers());
			Bean<?> bean = getBeanManager().resolve(beans);
			if (bean != null) {
				AnnotationCommandHandlerAdapter.subscribe(handler.getReference(getBeanManager()),
						commandBus);
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	protected <T extends EventSourcedAggregateRoot<?>> void registerRepositories(X commandBus) {
		final Set<Annotation> qualifiers = copyOf(getQualifiers());
		for (AggregateRootInfo aggregateRoot : aggregateRoots) {
			if (aggregateRoot.matchQualifiers(COMMAND_BUS, qualifiers)) {
				Class<T> aggregateType = (Class<T>) aggregateRoot.getType();
				ParameterizedType repositoryType = parameterize(EventSourcingRepository.class,
						aggregateType);
				EventSourcingRepository<T> repository = (EventSourcingRepository<T>) getReference(
						getBeanManager(), repositoryType, aggregateRoot.getQualifiers(REPOSITORY));
				subscribe(aggregateType, repository, commandBus);
			}
		}
	}
}
