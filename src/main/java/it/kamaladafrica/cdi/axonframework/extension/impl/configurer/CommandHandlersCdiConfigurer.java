package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.CommandHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class CommandHandlersCdiConfigurer extends AbstractCdiConfiguration {

	public CommandHandlersCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);

		// can't use lambda because of the checked exceptions thrown by *proxyCommandHandler.newInstance()*
		for (final CommandHandlerBeanInfo commandHandlerBeanInfo: executionContext.commandHandlerBeanInfos()) {
			// Use a proxy to get reference (because it is not created yet)
			// I can't use Proxy from jdk because a CommandHandler doesn't implement an interface.
			// Go for byte-buddy :)
			Class<?> proxyCommandHandler = new ByteBuddy()
				.subclass(commandHandlerBeanInfo.type())
				.method(ElementMatchers.any())
				.intercept(InvocationHandlerAdapter.of(new CommandHandlerInvocationHandler(beanManager, commandHandlerBeanInfo)))
				.make()
				.load(commandHandlerBeanInfo.type().getClassLoader())
				.getLoaded();
			Object instanceCommandHandler = proxyCommandHandler.newInstance();
			configurer.registerCommandHandler(c -> instanceCommandHandler);
		}

	}

	private class CommandHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final CommandHandlerBeanInfo commandHandlerBeanInfo;
		private Object commandHandler;

		public CommandHandlerInvocationHandler(final BeanManager beanManager, final CommandHandlerBeanInfo commandHandlerBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.commandHandlerBeanInfo = Objects.requireNonNull(commandHandlerBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (commandHandler == null) {
				commandHandler = commandHandlerBeanInfo.getReference(beanManager);
			}
			return method.invoke(commandHandler, args);
		}
		
	}

}
