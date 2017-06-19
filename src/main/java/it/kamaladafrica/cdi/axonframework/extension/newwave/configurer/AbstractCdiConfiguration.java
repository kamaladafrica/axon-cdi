package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

import org.axonframework.config.Configurer;

public abstract class AbstractCdiConfiguration implements AxonCdiConfigurer {

	private final AxonCdiConfigurer original;

	public AbstractCdiConfiguration(final AxonCdiConfigurer original) {
		this.original = Objects.requireNonNull(original);
	}

	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> qualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifiers);
		qualifiers.forEach(qualifier -> {
			if (qualifier.getClass().isAnnotationPresent(Qualifier.class)) {
				throw new IllegalArgumentException(String.format("'%s' is not a qualifier", qualifier.getClass().getSimpleName()));
			}
		});
		Configurer originalConfigurer = original.setUp(configurer, beanManager, qualifiers);
		concreateCdiSetUp(originalConfigurer, beanManager, qualifiers);
		return originalConfigurer;
	};

	protected abstract void concreateCdiSetUp(Configurer configurer, BeanManager beanManager, Set<Annotation> qualifiers) throws Exception;

}
