package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class AxonCdiConfigurationEntryPoint implements AxonCdiConfigurer {

	@Override
	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		return configurer;
	}

}
