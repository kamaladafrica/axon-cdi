package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CommandHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class CommandHandlersCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<CommandHandlerBeanInfo> commandHandlerBeanInfos;

	public CommandHandlersCdiConfigurer(final AxonCdiConfigurer original, final Set<CommandHandlerBeanInfo> commandHandlerInfos) {
		super(original);
		this.commandHandlerBeanInfos = Objects.requireNonNull(commandHandlerInfos);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		// can't use lambda because of the checked exceptions thrown by *proxyCommandHandler.newInstance()*
		for (final CommandHandlerBeanInfo commandHandlerBeanInfo: commandHandlerBeanInfos) {
			if (CdiUtils.qualifiersMatch(commandHandlerBeanInfo.normalizedQualifiers(), normalizedQualifiers)) {
				// Use a proxy to get reference (because it is not created yet)
				// I can't use Proxy from jdk because a CommandHandler doesn't implement an interface.
				// Go for byte-buddy :)
				Class<?> proxyCommandHandler = new ByteBuddy()
						  .subclass(commandHandlerBeanInfo.type())
						  .method(ElementMatchers.any())
						  .intercept(InvocationHandlerAdapter.of(new CommandHandlerInvocationHandler(beanManager, commandHandlerBeanInfo.type(), normalizedQualifiers)))
						  .make()
						  .load(commandHandlerBeanInfo.type().getClassLoader())
						  .getLoaded();
				Object instanceCommandHandler = proxyCommandHandler.newInstance();
				configurer.registerCommandHandler(c -> instanceCommandHandler);
			}
		}
	}

	private class CommandHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Type type;
		private final Set<Annotation> normalizedQualifiers;
		private Object commandHandler;

		public CommandHandlerInvocationHandler(final BeanManager beanManager, final Type type, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.type = Objects.requireNonNull(type);
			Objects.requireNonNull(normalizedQualifiers);
			this.normalizedQualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (commandHandler == null) {
				commandHandler = CdiUtils.getReference(beanManager, type, normalizedQualifiers);
			}
			return method.invoke(commandHandler, args);
		}
		
	}

}
