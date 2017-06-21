package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.EventHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

// cf. AxonAutoConfiguration : configureEventHandling
// also DefaultConfigurerTest : testRegisterSeveralModules
public class EventHandlersCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<EventHandlerBeanInfo> eventHandlerBeanInfos;

	public EventHandlersCdiConfigurer(final AxonCdiConfigurer original, final Set<EventHandlerBeanInfo> eventHandlerInfos) {
		super(original);
		this.eventHandlerBeanInfos = Objects.requireNonNull(eventHandlerInfos);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);

		EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
		for (final EventHandlerBeanInfo eventHandlerBeanInfo : eventHandlerBeanInfos) {
			if (aggregateRootBeanInfo.matchQualifiers(QualifierType.EVENT_BUS, eventHandlerBeanInfo.normalizedQualifiers())) {
				// use byte-buddy
				// cf. CommandHandlersCdiConfigurer for more information
				Class<?> proxyEventHandler = new ByteBuddy()
						  .subclass(eventHandlerBeanInfo.type())
						  .method(ElementMatchers.any())
						  .intercept(InvocationHandlerAdapter.of(new EventHandlerInvocationHandler(beanManager, eventHandlerBeanInfo)))
						  .make()
						  .load(eventHandlerBeanInfo.type().getClassLoader())
						  .getLoaded();
				Object instanceEventHandler = proxyEventHandler.newInstance();
				eventHandlingConfiguration.registerEventHandler(c -> instanceEventHandler);
				// By default we consider that we are tracking event handlers
				eventHandlingConfiguration.usingTrackingProcessors();
			}
		}
		configurer.registerModule(eventHandlingConfiguration);
	}

	private class EventHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final EventHandlerBeanInfo eventHandlerBeanInfo;
		private Object eventHandler;

		public EventHandlerInvocationHandler(final BeanManager beanManager, final EventHandlerBeanInfo eventHandlerBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.eventHandlerBeanInfo = Objects.requireNonNull(eventHandlerBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventHandler == null) {
				eventHandler = eventHandlerBeanInfo.getReference(beanManager);
			}
			return method.invoke(eventHandler, args);
		}
		
	}

}
