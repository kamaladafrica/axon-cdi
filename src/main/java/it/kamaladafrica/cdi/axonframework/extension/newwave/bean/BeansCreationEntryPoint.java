package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

public class BeansCreationEntryPoint implements BeansCreation {

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
			final BeanManager beanManager, final Set<Annotation> qualifiers, final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifiers);
		Objects.requireNonNull(configuration);
		// do nothing : just the entry point
	}

}
