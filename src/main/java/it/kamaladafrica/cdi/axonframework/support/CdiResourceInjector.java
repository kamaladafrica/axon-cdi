package it.kamaladafrica.cdi.axonframework.support;

import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.injectFields;
import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.saga.ResourceInjector;
import org.axonframework.saga.Saga;

public class CdiResourceInjector implements ResourceInjector {

	private final BeanManager beanManager;

	public CdiResourceInjector(BeanManager beanManager) {
		this.beanManager = requireNonNull(beanManager);
	}

	@Override
	public void injectResources(Saga saga) {
		injectFields(beanManager, saga);
	}

}
