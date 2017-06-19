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
import org.axonframework.serialization.Serializer;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class SerializerCdiConfigurer extends AbstractCdiConfiguration {

	public SerializerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Bean<?> bean = CdiUtils.getBean(beanManager, Serializer.class, normalizedQualifiers);
		if (bean != null) {
			Serializer serializer = (Serializer) Proxy.newProxyInstance(
					Serializer.class.getClassLoader(),
					new Class[] { Serializer.class },
					new SerializerInvocationHandler(beanManager, normalizedQualifiers));
			configurer.configureSerializer(c -> serializer);
		}
	}

	private class SerializerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private Serializer serializer;

		public SerializerInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (serializer == null) {
				serializer = (Serializer) CdiUtils.getReference(beanManager, Serializer.class, qualifiers);
			}
			return method.invoke(serializer, args);
		}

	}

}
