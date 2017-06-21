package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.CdiResourceInjector;

public class ResourceInjectorCdiConfigurer extends AbstractCdiConfiguration {

	public ResourceInjectorCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		configurer.configureResourceInjector(c -> new CdiResourceInjector(beanManager));
	}

}
