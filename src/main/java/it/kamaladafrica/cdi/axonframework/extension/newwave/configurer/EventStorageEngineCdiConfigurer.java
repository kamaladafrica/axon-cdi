package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class EventStorageEngineCdiConfigurer extends AbstractCdiConfiguration {

	public EventStorageEngineCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
		if (bean != null) {
			EventStorageEngine eventStorageEngine = (EventStorageEngine) Proxy.newProxyInstance(
					EventStorageEngine.class.getClassLoader(),
					new Class[] { EventStorageEngine.class },
					new EventStorageEngineInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.configureEmbeddedEventStore(c -> eventStorageEngine);
		}
	}

	private class EventStorageEngineInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private EventStorageEngine eventStorageEngine;

		public EventStorageEngineInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventStorageEngine == null) {
				eventStorageEngine = (EventStorageEngine) aggregateRootBeanInfo.getReference(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
			}
			return method.invoke(eventStorageEngine, args);
		}

	}

}
