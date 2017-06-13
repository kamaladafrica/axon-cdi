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

	Class<?> repository() default InheritQualifiers.class;

	Class<?> eventBus() default InheritQualifiers.class;

	Class<?> commandBus() default InheritQualifiers.class;

	Class<?> snapshotterTriggerDefinition() default InheritQualifiers.class;

	Class<?> snapshotter() default InheritQualifiers.class;

}
