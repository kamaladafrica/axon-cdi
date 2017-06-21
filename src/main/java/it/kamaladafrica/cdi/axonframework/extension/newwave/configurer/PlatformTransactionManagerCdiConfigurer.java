package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class PlatformTransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public PlatformTransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		// Nothing to implement
		// *PlatformTransactionManager* Spring specific interface
		// The code is the same than TransactionManagerCdiConfigurer but specific for Spring
	}

}
