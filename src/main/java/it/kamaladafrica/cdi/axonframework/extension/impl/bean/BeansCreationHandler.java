package it.kamaladafrica.cdi.axonframework.extension.impl.bean;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public interface BeansCreationHandler {

	/**
	 * All implementation (like EventStore, Repository...) must be retrieve from configuration.
	 * You must not used Cdi to retrieve bean reference.
	 * Implementation from configuration are proxied and it is the normal way
	 *
	 * @param afterBeanDiscovery
	 * @param beanManager
	 * @param executionContext
	 * @param configuration
	 */
	void create(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, ExecutionContext executionContext, Configuration configuration);

}
