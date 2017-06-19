package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

public class PlatformTransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public PlatformTransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		// Nothing to implement
		// *PlatformTransactionManager* Spring specific interface
		// The code is the same than TransactionManagerCdiConfigurer but specific for Spring
	}

}
