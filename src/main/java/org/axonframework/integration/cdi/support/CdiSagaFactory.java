package org.axonframework.integration.cdi.support;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.saga.GenericSagaFactory;

public class CdiSagaFactory extends GenericSagaFactory {

	public CdiSagaFactory(BeanManager beanManager) {
		setResourceInjector(new CdiResourceInjector(beanManager));
	}

}
