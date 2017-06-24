package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class CommandBusCdiConfigurer extends AbstractCdiConfiguration {

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	// Passage par proxy...

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (!executionContext.hasACommandBusBean(beanManager)) {
Je me suis planté
si je ne n'ai pas de bean je dois le creer
puis je configure
			CommandBus commandBus = (CommandBus) Proxy.newProxyInstance(
				CommandBus.class.getClassLoader(),
				new Class[] { CommandBus.class },
				new CommandBusInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.configureCommandBus(c -> commandBus);
		}
	}

	private class CommandBusInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private CommandBus commandBus;

		public CommandBusInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
// TODO checker quand est il appelé...
			if (commandBus == null) {
				commandBus = executionContext.getCommandBusReference(beanManager);
			}
			return method.invoke(commandBus, args);
		}

	}

}
