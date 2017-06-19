package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class EventStorageEngineCdiConfigurer extends AbstractCdiConfiguration {

	public EventStorageEngineCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Bean<?> bean = CdiUtils.getBean(beanManager, EventStorageEngine.class, normalizedQualifiers);
		if (bean != null) {
			EventStorageEngine eventStorageEngine = (EventStorageEngine) Proxy.newProxyInstance(
					EventStorageEngine.class.getClassLoader(),
					new Class[] { EventStorageEngine.class },
					new EventStorageEngineInvocationHandler(beanManager, normalizedQualifiers));
			configurer.configureEmbeddedEventStore(c -> eventStorageEngine);
		}
	}

	private class EventStorageEngineInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private EventStorageEngine eventStorageEngine;

		public EventStorageEngineInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventStorageEngine == null) {
				eventStorageEngine = (EventStorageEngine) CdiUtils.getReference(beanManager, EventStorageEngine.class, qualifiers);
			}
			return method.invoke(eventStorageEngine, args);
		}

	}

}
