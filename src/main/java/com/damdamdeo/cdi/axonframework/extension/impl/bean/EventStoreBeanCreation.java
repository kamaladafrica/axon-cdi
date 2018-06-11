package com.damdamdeo.cdi.axonframework.extension.impl.bean;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class EventStoreBeanCreation extends AbstractBeansCreationHandler {

	public EventStoreBeanCreation(final BeansCreationHandler original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final ExecutionContext executionContext,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		if (!executionContext.hasAnEventStoreBean(beanManager)) {
			BeanBuilder<EventStore> builder = new BeanBuilder<EventStore>(beanManager)
					.beanClass(EventStore.class)
					.qualifiers(executionContext.commandGatewayQualifiers())
					.types(EventStore.class)
					.scope(ApplicationScoped.class)
					.beanLifecycle(
						new EventStoreContextualLifeCycle(configuration));
			Bean<?> newCommandGatewayBeanToAdd = builder.create();
			return Collections.singleton(newCommandGatewayBeanToAdd);
		}
		return Collections.<Bean<?>> emptySet();
	}

	private class EventStoreContextualLifeCycle implements ContextualLifecycle<EventStore> {

		private final Configuration configuration;

		public EventStoreContextualLifeCycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		public EventStore create(final Bean<EventStore> bean, final CreationalContext<EventStore> creationalContext) {
			return (EventStore) configuration.eventStore();
		}

		@Override
		public void destroy(final Bean<EventStore> bean, final EventStore instance, final CreationalContext<EventStore> creationalContext) {
			creationalContext.release();
		}

	}

}
