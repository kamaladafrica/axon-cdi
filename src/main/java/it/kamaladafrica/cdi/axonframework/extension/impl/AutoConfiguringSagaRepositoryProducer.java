package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.support.CdiResourceInjector;

import java.lang.reflect.Method;

import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.axonframework.eventhandling.saga.ResourceInjector;
import org.axonframework.eventhandling.saga.SagaRepository;

public class AutoConfiguringSagaRepositoryProducer<X extends SagaRepository<?>> extends
		AbstractAutoConfiguringProducer<X> {

	private final static String RESOURCE_INJECTOR_SETTER = "setResourceInjector";

	public AutoConfiguringSagaRepositoryProducer(final Producer<X> wrappedProducer,
			final AnnotatedMember<?> annotatedMember, final BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
	}

	@Override
	protected X configure(final X repository) {
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
