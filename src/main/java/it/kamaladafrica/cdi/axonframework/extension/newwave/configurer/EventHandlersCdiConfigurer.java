package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.EventHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;
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
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
		for (final EventHandlerBeanInfo eventHandlerBeanInfo : eventHandlerBeanInfos) {
			if (CdiUtils.qualifiersMatch(eventHandlerBeanInfo.normalizedQualifiers(), normalizedQualifiers)) {
				Bean<?> eventHandlerBean = CdiUtils.getBean(beanManager, eventHandlerBeanInfo.type(), normalizedQualifiers);
				if (eventHandlerBean != null) {
					// use byte-buddy
					// cf. CommandHandlersCdiConfigurer for more information
					Class<?> proxyEventHandler = new ByteBuddy()
							  .subclass(eventHandlerBeanInfo.type())
							  .method(ElementMatchers.any())
							  .intercept(InvocationHandlerAdapter.of(new EventHandlerInvocationHandler(beanManager, eventHandlerBeanInfo.type(), normalizedQualifiers)))
							  .make()
							  .load(eventHandlerBeanInfo.type().getClassLoader())
							  .getLoaded();
					Object instanceEventHandler = proxyEventHandler.newInstance();
					eventHandlingConfiguration.registerEventHandler(c -> instanceEventHandler);
					// By default we consider that we are tracking event handlers
					eventHandlingConfiguration.usingTrackingProcessors();
				}
			}
		}
		configurer.registerModule(eventHandlingConfiguration);
	}

	private class EventHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Type type;
		private final Set<Annotation> normalizedQualifiers;
		private Object eventHandler;

		public EventHandlerInvocationHandler(final BeanManager beanManager, final Type type, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.type = Objects.requireNonNull(type);
			Objects.requireNonNull(normalizedQualifiers);
			this.normalizedQualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventHandler == null) {
				eventHandler = CdiUtils.getReference(beanManager, type, normalizedQualifiers);
			}
			return method.invoke(eventHandler, args);
		}
		
	}

}
