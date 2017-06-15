package it.kamaladafrica.cdi.axonframework.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.Priority;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.ParameterResolver;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParameterResolverFactory implementation that resolves parameters in the CDI Context.
 * A parameter can be resolved as a CDI bean if there is exactly one bean assignable to
 * the parameter type.
 */
@Priority(Priority.LOW)
public class CdiParameterResolverFactory implements ParameterResolverFactory {

	private static final Logger logger = LoggerFactory.getLogger(CdiParameterResolverFactory.class);

	private final BeanManager beanManager;

	public CdiParameterResolverFactory(final BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	@Override
	public ParameterResolver<Object> createInstance(final Executable executable, final Parameter[] parameters, final int parameterIndex) {
		Class<?> parameterType = parameters[parameterIndex].getType();
		Annotation[] annotations = parameters[parameterIndex].getAnnotations();
		Set<Annotation> qualifiers = CdiUtils.qualifiers(beanManager, annotations);
		Set<Bean<?>> beansFound = CdiUtils.getBeans(beanManager, parameterType, qualifiers);
		if (beansFound.isEmpty()) {
			return null;
		} else if (beansFound.size() > 1) {
			if (logger.isWarnEnabled()) {
				logger.warn("Ambiguous reference for parameter type {} with qualifiers {}",
						parameterType.getName(), qualifiers);
			}
			return null;
		} else {
			return new CdiParameterResolver(beanManager, beansFound.iterator().next(), parameterType);
		}
	}

	private static class CdiParameterResolver implements ParameterResolver<Object> {

		private final BeanManager beanManager;

		private final Bean<?> bean;

		private final Type type;

		public CdiParameterResolver(final BeanManager beanManager, final Bean<?> bean, final Type type) {
			this.beanManager = beanManager;
			this.bean = bean;
			this.type = type;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object resolveParameterValue(final Message message) {
			return CdiUtils.getReference(beanManager, bean, type);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean matches(Message message) {
			return true;
		}
	}

}
