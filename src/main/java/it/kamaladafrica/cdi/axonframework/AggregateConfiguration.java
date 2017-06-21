package it.kamaladafrica.cdi.axonframework;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface AggregateConfiguration {

	Class<?> value() default InheritQualifiers.class;

	Class<?> eventBus() default InheritQualifiers.class;

	Class<?> commandBus() default InheritQualifiers.class;

	Class<?> commandGateway() default InheritQualifiers.class;
	
	Class<?> snapshotterTriggerDefinition() default InheritQualifiers.class;

	Class<?> tokenStore() default InheritQualifiers.class;

	Class<?> transactionManager() default InheritQualifiers.class;

	Class<?> serializer() default InheritQualifiers.class;

	Class<?> eventStorageEngine() default InheritQualifiers.class;

	Class<?> eventScheduler() default InheritQualifiers.class;

	Class<?> correlationDataProvider() default InheritQualifiers.class;

}
