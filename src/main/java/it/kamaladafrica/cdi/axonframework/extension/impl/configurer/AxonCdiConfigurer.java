package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public interface AxonCdiConfigurer {

	Configurer setUp(Configurer configurer, BeanManager beanManager, ExecutionContext executionContext) throws Exception;

}
