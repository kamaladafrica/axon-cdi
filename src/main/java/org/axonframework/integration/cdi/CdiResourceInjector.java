package org.axonframework.integration.cdi;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.axonframework.saga.ResourceInjector;
import org.axonframework.saga.Saga;

public class CdiResourceInjector implements ResourceInjector {

	@Override
	public void injectResources(Saga saga) {
		BeanProvider.injectFields(saga);
	}

}
