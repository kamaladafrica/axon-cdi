package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class PlatformTransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public PlatformTransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		// Nothing to implement
		// *PlatformTransactionManager* Spring specific interface
		// The code is the same than TransactionManagerCdiConfigurer but specific for Spring
	}

}
