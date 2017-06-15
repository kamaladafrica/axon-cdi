package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.axonframework.commandhandling.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.AnnotationCommandHandlerAdapter;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.eventsourcing.EventSourcingRepository;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

// Configure commandHandler on aggregate and beans
// Cf AggregateConfigurer
public class AutoConfiguringCommandBusProducer<X extends CommandBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> commandHandlersInfo;

	private final Set<AggregateRootInfo> aggregateRootsInfo;

	public AutoConfiguringCommandBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<AggregateRootInfo> aggregateRootsInfo,
			Set<HandlerInfo> commandHandlersInfo,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.commandHandlersInfo = commandHandlersInfo;
		this.aggregateRootsInfo = aggregateRootsInfo;
	}

	@Override
	protected X configure(X commandBus) {
		registerCommandHandlers(commandBus);
		registerRepositories(commandBus);
		return commandBus;
	}

	private void registerCommandHandlers(X commandBus) {
		for (HandlerInfo commandHandlerInfo : commandHandlersInfo) {
			Set<Bean<?>> beans = getBeanManager().getBeans(commandHandlerInfo.getType(), getQualifiers());
			Bean<?> bean = getBeanManager().resolve(beans);
			if (bean != null) {
				AnnotationCommandHandlerAdapter annotationCommandHandlerAdapter = new AnnotationCommandHandlerAdapter(commandHandlerInfo.getReference(getBeanManager()));
				annotationCommandHandlerAdapter.subscribe(commandBus);
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	protected <T> void registerRepositories(X commandBus) {
		final Set<Annotation> qualifiers = ImmutableSet.copyOf(getQualifiers());
		for (AggregateRootInfo aggregateRootInfo : aggregateRootsInfo) {
			if (aggregateRootInfo.matchQualifiers(QualifierType.COMMAND_BUS, qualifiers)) {
				Class<T> aggregateType = (Class<T>) aggregateRootInfo.getType();
				ParameterizedType repositoryType = TypeUtils.parameterize(EventSourcingRepository.class,
						aggregateType);
				EventSourcingRepository<T> repository = (EventSourcingRepository<T>) CdiUtils.getReference(
						getBeanManager(), repositoryType, aggregateRootInfo.getQualifiers(QualifierType.REPOSITORY));
				AggregateAnnotationCommandHandler<?> aggregateAnnotationCommandHandler = new AggregateAnnotationCommandHandler<>(aggregateType,
						repository);
				aggregateAnnotationCommandHandler.subscribe(commandBus);
				if (commandBus instanceof DisruptorCommandBus) {
					((DisruptorCommandBus) commandBus).createRepository(repository.getAggregateFactory());
				}
			}
		}
	}

}
