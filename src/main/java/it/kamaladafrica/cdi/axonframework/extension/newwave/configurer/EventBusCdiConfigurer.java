package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Executors;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.axonframework.eventsourcing.eventstore.EventStore;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

// Remember the EventStore is the EventBus
public class EventBusCdiConfigurer extends AbstractCdiConfiguration {

	public EventBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.EVENT_BUS);
		if (bean != null) {
			// must cast to EventStore
			// or "c.eventBus() instanceof EventStore" in AggregateConfigurer will return false and throw an exception
			EventStore eventStore = (EventStore) Proxy.newProxyInstance(
					EventStore.class.getClassLoader(),
					new Class[] { EventStore.class },
					new EventStoreInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.configureEventBus(c -> eventStore);
			// I don't know where to init EventScheduler ... so I do it here
			new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(), eventStore);
		}
	}

	private class EventStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private EventStore eventStore;

		public EventStoreInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventStore == null) {
				eventStore = (EventStore) aggregateRootBeanInfo.getReference(beanManager, QualifierType.EVENT_BUS);
			}
			return method.invoke(eventStore, args);
		}

	}

}
