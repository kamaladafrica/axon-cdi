package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.support.CdiResourceInjector;

public class ResourceInjectorCdiConfigurer extends AbstractCdiConfiguration {

	public ResourceInjectorCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		configurer.configureResourceInjector(c -> new CdiResourceInjector(beanManager));
	}

}
