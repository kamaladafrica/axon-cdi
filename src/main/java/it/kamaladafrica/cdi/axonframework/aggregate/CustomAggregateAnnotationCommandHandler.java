package it.kamaladafrica.cdi.axonframework.aggregate;

import java.util.function.Function;

import org.axonframework.commandhandling.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.AnnotationCommandTargetResolver;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandTargetResolver;
import org.axonframework.commandhandling.model.Aggregate;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

public class CustomAggregateAnnotationCommandHandler<T> extends AggregateAnnotationCommandHandler<T> {

	public CustomAggregateAnnotationCommandHandler(final Class<T> aggregateType, final Repository<T> repository) {
		this(aggregateType, repository, new AnnotationCommandTargetResolver());
	}

	public CustomAggregateAnnotationCommandHandler(final Class<T> aggregateType, final Repository<T> repository,
			CommandTargetResolver commandTargetResolver) {
		this(aggregateType, repository, commandTargetResolver,
				ClasspathParameterResolverFactory.forClass(aggregateType));
	}

	public CustomAggregateAnnotationCommandHandler(final Class<T> aggregateType,
			final Repository<T> repository,
			final CommandTargetResolver commandTargetResolver,
			final ParameterResolverFactory parameterResolverFactory) {
		super(aggregateType, repository, commandTargetResolver, parameterResolverFactory);
	}

	// The original AggregateAnnotationCommandHandler return only the id of the createdAggregate.
	// But why ? I wan't the model and in my hexagonal architecture I will be able to translate
	// to a dto ;)
	@Override
	protected Object resolveReturnValue(CommandMessage<?> command, Aggregate<T> createdAggregate) {
		return createdAggregate.invoke(new Function<T, T>() {

			@Override
			public T apply(T aggregateRoot) {
				return aggregateRoot;
			}

		});
	}

}
