package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class CommandBusCdiConfigurer extends AbstractCdiConfiguration {

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	// Passage par proxy...

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.COMMAND_BUS);
		if (bean != null) {
			CommandBus commandBus = (CommandBus) Proxy.newProxyInstance(
					CommandBus.class.getClassLoader(),
					new Class[] { CommandBus.class },
					new CommandBusInvocationHandler(beanManager, aggregateRootBeanInfo));

			configurer.configureCommandBus(c -> commandBus);
		}
	}

	private class CommandBusInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private CommandBus commandBus;

		public CommandBusInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
// TODO checker quand est il appelé...
			if (commandBus == null) {
				commandBus = (CommandBus) aggregateRootBeanInfo.getReference(beanManager, QualifierType.COMMAND_BUS);
			}
			return method.invoke(commandBus, args);
		}

	}

}
