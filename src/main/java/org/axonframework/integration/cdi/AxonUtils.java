package org.axonframework.integration.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.util.TypeLiteral;

import org.apache.deltaspike.core.util.ExceptionUtils;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.domain.AggregateRoot;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;

public class AxonUtils {

	public static boolean isAnnotatedAggregateRoot(Class<?> targetClass) {
		return EventSourcedAggregateRoot.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isAnnotatedSaga(Class<?> targetClass) {
		return AbstractAnnotatedSaga.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isCommandHandler(Class<?> targetClass) {
		return isNotCommandHandlerSubclass(targetClass) && hasCommandHandlerMethod(targetClass);
	}

	public static boolean isCommandBus(Class<?> targetClass) {
		return CommandBus.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isEventBus(Class<?> targetClass) {
		return EventBus.class.isAssignableFrom(targetClass)
				&& !Modifier.isAbstract(targetClass.getModifiers());
	}

	public static boolean isEventHandler(Class<?> targetClass) {
		return isNotAggregateRoot(targetClass) && isNotEventHandlerSubclass(targetClass)
				&& hasEventHandlerMethod(targetClass);
	}

	public static boolean isNotCommandHandlerSubclass(Class<?> beanClass) {
		return !CommandHandler.class.isAssignableFrom(beanClass)
				&& !AggregateRoot.class.isAssignableFrom(beanClass);
	}

	public static boolean hasCommandHandlerMethod(Class<?> beanClass) {
		final AtomicBoolean result = new AtomicBoolean(false);
		doWithMethods(beanClass, new HasCommandHandlerAnnotationMethodCallback(
				result));
		return result.get();
	}

	public static boolean isNotAggregateRoot(Class<?> targetClass) {
		return !AggregateRoot.class.isAssignableFrom(targetClass);
	}

	public static boolean isNotEventHandlerSubclass(Class<?> beanClass) {
		return !EventListener.class.isAssignableFrom(beanClass);
	}

	public static boolean hasEventHandlerMethod(Class<?> beanClass) {
		final AtomicBoolean result = new AtomicBoolean(false);
		doWithMethods(beanClass,
				new HasEventHandlerAnnotationMethodCallback(result));
		return result.get();
	}

	public static <T> TypeLiteral<T> asTypeLiteral(Type type) {
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

	private static final class HasCommandHandlerAnnotationMethodCallback implements
			MethodCallback {

		private final AtomicBoolean result;

		private HasCommandHandlerAnnotationMethodCallback(AtomicBoolean result) {
			this.result = result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
			if (method
					.isAnnotationPresent(
							org.axonframework.commandhandling.annotation.CommandHandler.class)) {
				result.set(true);
			}
		}
	}

	private static final class HasEventHandlerAnnotationMethodCallback implements
			MethodCallback {

		private final AtomicBoolean result;

		private HasEventHandlerAnnotationMethodCallback(AtomicBoolean result) {
			this.result = result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
			if (method.isAnnotationPresent(EventHandler.class)) {
				result.set(true);
			}
		}
	}

	private static void doWithMethods(Class<?> clazz, MethodCallback mc)
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

}
