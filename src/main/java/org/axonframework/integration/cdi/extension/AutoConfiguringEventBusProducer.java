package org.axonframework.integration.cdi.extension;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Producer;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.saga.SagaManager;

public class AutoConfiguringEventBusProducer<X extends EventBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final List<Class<?>> handlerClasses;

	public AutoConfiguringEventBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			List<Class<?>> handlerClasses,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlerClasses = handlerClasses;
	}

	@Override
	protected X configure(X eventBus) {
		for (Class<?> handlerClass : handlerClasses) {
			Object handler = CDI.current().select(handlerClass, getQualifiers()).get();
			AnnotationEventListenerAdapter.subscribe(handler, eventBus);
		}
		registerSagaManager(eventBus);
		return eventBus;
	}

	private void registerSagaManager(X eventBus) {
		Instance<SagaManager> managerInstance = CDI.current().select(SagaManager.class);
		if (managerInstance.isAmbiguous()) {
			managerInstance = managerInstance.select(getQualifiers());
		}
		if (!managerInstance.isUnsatisfied()) {
			eventBus.subscribe(managerInstance.get());
		}
	}

}
