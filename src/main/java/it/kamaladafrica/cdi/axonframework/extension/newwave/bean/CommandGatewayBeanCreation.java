package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class CommandGatewayBeanCreation extends AbstractBeanCreation {

	public CommandGatewayBeanCreation(final BeanCreation original) {
		super(original);
	}

	@Override
	protected Bean<?> concreateCreateBean(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Objects.requireNonNull(configuration);
		if (aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.COMMAND_GATEWAY) == null) {
			// No CommandGateway has been Produces so create a default one from *configuration* ^^
			// It can be injected in facade next :)
			BeanBuilder<CommandGateway> builder = new BeanBuilder<CommandGateway>(beanManager)
				.beanClass(CommandGateway.class)
				.qualifiers(aggregateRootBeanInfo.qualifiers(QualifierType.COMMAND_GATEWAY))
				.types(CommandGateway.class)
				.scope(ApplicationScoped.class)
				.beanLifecycle(
					new CommandGatewayContextualLifeCycle<CommandGateway>(configuration));
			Bean<?> commandGatewayBean = builder.create();
			return commandGatewayBean;
		}
		return null;
	}

	private class CommandGatewayContextualLifeCycle<T extends CommandGateway> implements ContextualLifecycle<T> {

		private final Configuration configuration;

		public CommandGatewayContextualLifeCycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
			return (T) configuration.commandGateway();
		}

		@Override
		public void destroy(final Bean<T> bean, final T instance, final CreationalContext<T> creationalContext) {
			creationalContext.release();
		}

	}

}
