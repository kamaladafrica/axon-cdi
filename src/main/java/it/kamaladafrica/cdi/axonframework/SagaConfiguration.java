package it.kamaladafrica.cdi.axonframework;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface SagaConfiguration {

	Class<?> value() default DefaultQualifierMeme.class;

	Class<?> repository() default DefaultQualifierMeme.class;

	Class<?> factory() default DefaultQualifierMeme.class;

	Class<?> eventBus() default DefaultQualifierMeme.class;

}
