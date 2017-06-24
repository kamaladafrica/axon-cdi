package it.kamaladafrica.cdi.axonframework.extension.impl.bean;

import java.util.Objects;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class BeanCreationEntryPoint implements BeansCreationHandler {

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
			final BeanManager beanManager, final ExecutionContext executionContext, final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		// do nothing : just the entry point
	}

}
