package org.axonframework.integration.cdi.extension.impl;

import java.lang.reflect.Method;

import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.axonframework.integration.cdi.support.CdiResourceInjector;
import org.axonframework.saga.ResourceInjector;
import org.axonframework.saga.SagaRepository;

public class AutoConfiguringSagaRepositoryProducer<X extends SagaRepository> extends
		AbstractAutoConfiguringProducer<X> {

	private final static String RESOURCE_INJECTOR_SETTER = "setResourceInjector";

	public AutoConfiguringSagaRepositoryProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember, BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
	}

	@Override
	protected X configure(X repository) {
		Method setter = MethodUtils.getAccessibleMethod(repository.getClass(),
				RESOURCE_INJECTOR_SETTER,
				ResourceInjector.class);
		if (setter != null) {
			try {
				setter.invoke(repository, new CdiResourceInjector(getBeanManager()));
			} catch (ReflectiveOperationException e) {
				throw new CreationException(e);
			}
		}
		return repository;
	}

}
