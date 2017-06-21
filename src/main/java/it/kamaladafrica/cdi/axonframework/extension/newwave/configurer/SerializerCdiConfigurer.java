package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.serialization.Serializer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class SerializerCdiConfigurer extends AbstractCdiConfiguration {

	public SerializerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.SERIALIZER);
		if (bean != null) {
			Serializer serializer = (Serializer) Proxy.newProxyInstance(
					Serializer.class.getClassLoader(),
					new Class[] { Serializer.class },
					new SerializerInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.configureSerializer(c -> serializer);
		}
	}

	private class SerializerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private Serializer serializer;

		public SerializerInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (serializer == null) {
				serializer = (Serializer) aggregateRootBeanInfo.getReference(beanManager, QualifierType.SERIALIZER);
			}
			return method.invoke(serializer, args);
		}

	}

}
