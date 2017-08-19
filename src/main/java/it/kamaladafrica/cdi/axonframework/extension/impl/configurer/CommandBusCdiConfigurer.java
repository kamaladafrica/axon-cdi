package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class CommandBusCdiConfigurer extends AbstractCdiConfiguration {

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (executionContext.hasACommandBusBean(beanManager)) {
			Class<? extends CommandBus> proxyCommandBus = new ByteBuddy()
					.subclass(CommandBus.class)
					.method(ElementMatchers.any())
					.intercept(InvocationHandlerAdapter.of(new CommandBusInvocationHandler(beanManager, executionContext)))
					.make()
					.load(CommandBus.class.getClassLoader())
					.getLoaded();
			CommandBus instanceCommandBus = proxyCommandBus.newInstance();
			configurer.configureCommandBus(c -> instanceCommandBus);
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
			if (commandBus == null) {
				commandBus = executionContext.getCommandBusReference(beanManager);
			}
			return method.invoke(commandBus, args);
		}

	}

}
