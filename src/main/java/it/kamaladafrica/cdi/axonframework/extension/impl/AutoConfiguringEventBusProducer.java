package it.kamaladafrica.cdi.axonframework.extension.impl;

import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.getBeans;
import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.getReference;
import static org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter.subscribe;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.saga.SagaManager;

import com.google.common.collect.ImmutableSet;

public class AutoConfiguringEventBusProducer<X extends EventBus> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> handlers;

	private final Set<SagaManagerInfo> sagaManagers;

	public AutoConfiguringEventBusProducer(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember,
			Set<HandlerInfo> handlers,
			Set<SagaInfo> sagas,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.handlers = handlers;
		this.sagaManagers = SagaManagerInfo.from(sagas);
	}

	@Override
	protected X configure(X eventBus) {
		registerEventHandlers(eventBus);
		registerSagaManager(eventBus);
		return eventBus;
	}

	private void registerEventHandlers(X eventBus) {
		for (HandlerInfo handler : handlers) {
			Set<Bean<?>> beans = getBeanManager().getBeans(handler.getType(), getQualifiers());
			Bean<?> bean = getBeanManager().resolve(beans);
			if (bean != null) {
				subscribe(handler.getReference(getBeanManager()), eventBus);
			}
		}
	}

	private void registerSagaManager(X eventBus) {
		final Set<Annotation> qualifiers = ImmutableSet.copyOf(getQualifiers());
		for (SagaManagerInfo sagaManager : sagaManagers) {
			if (CdiUtils.qualifiersMatch(sagaManager.getEventBusQualifiers(), qualifiers)) {
				Set<Bean<?>> beans = getBeans(getBeanManager(), SagaManager.class,
						sagaManager.getRepositoryQualifiers());
				Bean<?> bean = getBeanManager().resolve(beans);
				if (bean != null) {
					eventBus.subscribe(
							(SagaManager) getReference(getBeanManager(), bean, SagaManager.class));
				}
			}
		}
	}

}
