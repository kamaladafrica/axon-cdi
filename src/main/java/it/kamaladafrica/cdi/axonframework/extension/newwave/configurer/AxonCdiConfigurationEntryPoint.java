package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

public class AxonCdiConfigurationEntryPoint implements AxonCdiConfigurer {

	@Override
	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> qualifiers) {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifiers);
		return configurer;
	}

}
