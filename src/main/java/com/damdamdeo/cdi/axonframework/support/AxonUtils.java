package com.damdamdeo.cdi.axonframework.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.util.ExceptionUtils;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

public class AxonUtils {

	public static boolean isAnnotatedAggregateRoot(final Class<?> targetClass) {
		return targetClass.isAnnotationPresent(AggregateRoot.class)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isAnnotatedSaga(final Class<?> targetClass) {
		// check if @StartSaga is present on a method
		final AtomicBoolean result = new AtomicBoolean(false);
		doWithMethods(targetClass, new HasStartSagaAnnotationMethodCallback(
				result));
		return result.get()
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isCommandHandlerBean(final Class<?> targetClass) {
		return isNotAggregateRoot(targetClass) && hasCommandHandlerMethod(targetClass);
	}

	public static boolean isCommandBus(final Class<?> targetClass) {
		return CommandBus.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isEventBus(final Class<?> targetClass) {
		return EventBus.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isEventHandlerBean(final Class<?> targetClass) {
		return isNotAggregateRoot(targetClass) && isNotEventHandlerSubclass(targetClass)
				&& hasEventHandlerMethod(targetClass);
	}

	public static boolean isCorrelationDataProvider(final Class<?> targetClass) {
		return CorrelationDataProvider.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isCommandGateway(final Class<?> targetClass) {
		return CommandGateway.class.isAssignableFrom(targetClass);
	}

	public static boolean hasCommandHandlerMethod(final Class<?> beanClass) {
		final AtomicBoolean result = new AtomicBoolean(false);
		doWithMethods(beanClass, new HasCommandHandlerAnnotationMethodCallback(
				result));
		return result.get();
	}

	public static boolean isNotAggregateRoot(final Class<?> targetClass) {
		return !targetClass.isAnnotationPresent(AggregateRoot.class);
	}

	public static boolean isNotEventHandlerSubclass(final Class<?> beanClass) {
		return !EventListener.class.isAssignableFrom(beanClass);
	}

	public static boolean hasEventHandlerMethod(final Class<?> beanClass) {
		final AtomicBoolean result = new AtomicBoolean(false);
		doWithMethods(beanClass,
				new HasEventHandlerAnnotationMethodCallback(result));
		return result.get();
	}

	public static <T> TypeLiteral<T> asTypeLiteral(final Type type) {
		@SuppressWarnings("serial")
		TypeLiteral<T> literal = new TypeLiteral<T>() {};
		for (Field field : TypeLiteral.class.getDeclaredFields()) {
			if (field.getType().equals(Type.class)) {
				try {
					field.setAccessible(true);
					field.set(literal, type);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
		}
		return literal;
	}

	private static final class HasStartSagaAnnotationMethodCallback implements MethodCallback {

		private final AtomicBoolean result;

		private HasStartSagaAnnotationMethodCallback(final AtomicBoolean result) {
			this.result = result;
		}

		@Override
		public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
			if (method
					.isAnnotationPresent(
							org.axonframework.eventhandling.saga.StartSaga.class)) {
				result.set(true);
			}
		}
		
	}

	private static final class HasCommandHandlerAnnotationMethodCallback implements
			MethodCallback {

		private final AtomicBoolean result;

		private HasCommandHandlerAnnotationMethodCallback(final AtomicBoolean result) {
			this.result = result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
			if (method
					.isAnnotationPresent(
							org.axonframework.commandhandling.CommandHandler.class)) {
				result.set(true);
			}
		}
	}

	private static final class HasEventHandlerAnnotationMethodCallback implements
			MethodCallback {

		private final AtomicBoolean result;

		private HasEventHandlerAnnotationMethodCallback(final AtomicBoolean result) {
			this.result = result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
			if (method.isAnnotationPresent(EventHandler.class)) {
				result.set(true);
			}
		}
	}

	private static void doWithMethods(final Class<?> clazz, final MethodCallback mc)
			throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			try {
				mc.doWith(method);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Shouldn't be illegal to access method '"
						+ method.getName() + "': " + ex);
			}
		}
		if (clazz.getSuperclass() != null) {
			doWithMethods(clazz.getSuperclass(), mc);
		} else if (clazz.isInterface()) {
			for (Class<?> superIfc : clazz.getInterfaces()) {
				doWithMethods(superIfc, mc);
			}
		}
	}

	private interface MethodCallback {

		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;

	}

	public static String lcFirst(final String string) {
		Objects.requireNonNull(string);
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

}
