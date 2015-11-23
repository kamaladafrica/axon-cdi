package org.axonframework.integration.cdi;

import java.util.Set;

import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;

public interface AxonCdiHelper {

	Set<Class<?>> getCommandHandlerClasses();

	Set<Class<?>> getEventHandlerClasses();

	Set<Class<? extends EventSourcedAggregateRoot<?>>> getAnnotatedAggregateRootClasses();

	Set<Class<? extends AbstractAnnotatedSaga>> getAnnotatedSagaClasses();
}
