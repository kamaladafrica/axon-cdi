package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.util.Objects;
import java.util.concurrent.Executors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;

public class SimpleEventSchedulerContextualLifecycle<T extends EventScheduler> implements ContextualLifecycle<T> {

	private final EventSchedulerInfo eventSchedulerInfo;

	private final BeanManager beanManager;

	public SimpleEventSchedulerContextualLifecycle(final BeanManager beanManager, final EventSchedulerInfo eventSchedulerInfo) {
		this.beanManager = Objects.requireNonNull(beanManager);
		this.eventSchedulerInfo = Objects.requireNonNull(eventSchedulerInfo);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
		EventBus eventBus = (EventBus) eventSchedulerInfo.getEventStoreReference(beanManager);
		return (T) new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(),
				eventBus);
	}

	@Override
	public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext) {
		creationalContext.release();
	}

}
