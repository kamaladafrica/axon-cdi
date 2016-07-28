package org.axonframework.integration.cdi.extension.impl;

import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.integration.cdi.support.CdiUtils;
import org.axonframework.saga.SagaManager;

public class AutoConfiguringEventBusProducer<X extends EventBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> handlers;

	public AutoConfiguringEventBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<HandlerInfo> handlers,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlers = handlers;
	}

	@Override
	protected X configure(X eventBus) {
		registerEventHandlers(eventBus);
		registerSagaManager(eventBus);
		return eventBus;
	}

	private void registerEventHandlers(X eventBus) {
		for (HandlerInfo handler : handlers) {
			AnnotationEventListenerAdapter.subscribe(handler.getReference(getBeanManager()),
					eventBus);
		}
	}

	private void registerSagaManager(X eventBus) {
		Set<Bean<?>> beans = getBeanManager().getBeans(SagaManager.class, getQualifiers());
		Bean<?> bean = getBeanManager().resolve(beans);
		if (bean != null) {
			eventBus.subscribe(
					(SagaManager) CdiUtils.getReference(getBeanManager(), bean, SagaManager.class));
		}
	}

}
