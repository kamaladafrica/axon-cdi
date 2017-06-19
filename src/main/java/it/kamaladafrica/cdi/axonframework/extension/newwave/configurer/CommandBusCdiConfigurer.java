package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class CommandBusCdiConfigurer extends AbstractCdiConfiguration {

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	// Passage par proxy...

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Bean<?> bean = CdiUtils.getBean(beanManager, CommandBus.class, normalizedQualifiers);
		if (bean != null) {
			CommandBus commandBus = (CommandBus) Proxy.newProxyInstance(
					CommandBus.class.getClassLoader(),
					new Class[] { CommandBus.class },
					new CommandBusInvocationHandler(beanManager, normalizedQualifiers));

			configurer.configureCommandBus(c -> commandBus);
		}
	}

	private class CommandBusInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private CommandBus commandBus;

		public CommandBusInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
// TODO checker quand est il appelé...
			if (commandBus == null) {
				commandBus = (CommandBus) CdiUtils.getReference(beanManager, CommandBus.class, qualifiers);
			}
			return method.invoke(commandBus, args);
		}

	}

}
