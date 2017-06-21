package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class ParameterResolverCdiConfigurer extends AbstractCdiConfiguration {

	public ParameterResolverCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
// Registering this component lead to an exception:
// org.axonframework.messaging.annotation.UnsupportedHandlerException: Unable to resolver parameter 0
// So I don't use it
//		configurer.registerComponent(ParameterResolverFactory.class, c -> new CdiParameterResolverFactory(beanManager));
	}

}
