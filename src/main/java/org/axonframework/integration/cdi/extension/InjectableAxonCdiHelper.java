package org.axonframework.integration.cdi.extension;

import java.util.Objects;
import java.util.Set;

import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.integration.cdi.AxonCdiHelper;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;

public class InjectableAxonCdiHelper implements AxonCdiHelper {

	private AxonCdiExtension extension;

	InjectableAxonCdiHelper(AxonCdiExtension extension) {
		this.extension = Objects.requireNonNull(extension);
	}

	@Override
	public Set<Class<?>> getCommandHandlerClasses() {
		return extension.getCommandHandlerClasses();
	}

	@Override
	public Set<Class<?>> getEventHandlerClasses() {
		return extension.getEventHandlerClasses();
	}

	@Override
	public Set<Class<? extends EventSourcedAggregateRoot<?>>> getAnnotatedAggregateRootClasses() {
		return extension.getAnnotatedAggregateRootClasses();
	}

	@Override
	public Set<Class<? extends AbstractAnnotatedSaga>> getAnnotatedSagaClasses() {
		return extension.getAnnotatedSagaClasses();
	}
}
