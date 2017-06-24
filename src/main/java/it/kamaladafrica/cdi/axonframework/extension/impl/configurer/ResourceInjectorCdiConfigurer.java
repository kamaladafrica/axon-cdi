package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import it.kamaladafrica.cdi.axonframework.support.CdiResourceInjector;

public class ResourceInjectorCdiConfigurer extends AbstractCdiConfiguration {

	public ResourceInjectorCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		// only one can be registered per configurer
		configurer.configureResourceInjector(c -> new CdiResourceInjector(beanManager));
	}

}
