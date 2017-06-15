package it.kamaladafrica.cdi.axonframework.support;

import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.injectFields;
import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.eventhandling.saga.ResourceInjector;

public class CdiResourceInjector implements ResourceInjector {

	private final BeanManager beanManager;

	public CdiResourceInjector(final BeanManager beanManager) {
		this.beanManager = requireNonNull(beanManager);
	}

	@Override
	public void injectResources(final Object saga) {
		injectFields(beanManager, saga);
	}

}
