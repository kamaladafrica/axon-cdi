package org.axonframework.integration.cdi;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface SagaConfiguration {

	Class<?> value() default DefaultQualifier.class;

	Class<?> repository() default DefaultQualifier.class;

	Class<?> factory() default DefaultQualifier.class;

	Class<?> eventBus() default DefaultQualifier.class;

}
