package com.damdamdeo.cdi.axonframework.extension.impl.bean;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class CommandGatewayBeanCreation extends AbstractBeansCreationHandler {

	public CommandGatewayBeanCreation(final BeansCreationHandler original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final ExecutionContext executionContext,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		if (!executionContext.hasACommandGatewayBean(beanManager)) {
			BeanBuilder<CommandGateway> builder = new BeanBuilder<CommandGateway>(beanManager)
					.beanClass(CommandGateway.class)
					.qualifiers(executionContext.commandGatewayQualifiers())
					.types(CommandGateway.class)
					.scope(ApplicationScoped.class)
					.beanLifecycle(
						new CommandGatewayContextualLifeCycle(configuration));
			Bean<?> newCommandGatewayBeanToAdd = builder.create();
			return Collections.singleton(newCommandGatewayBeanToAdd);
		}
		return Collections.<Bean<?>> emptySet();
	}

	private class CommandGatewayContextualLifeCycle implements ContextualLifecycle<CommandGateway> {

		private final Configuration configuration;

		public CommandGatewayContextualLifeCycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		public CommandGateway create(final Bean<CommandGateway> bean, final CreationalContext<CommandGateway> creationalContext) {
			return (CommandGateway) configuration.commandGateway();
		}

		@Override
		public void destroy(final Bean<CommandGateway> bean, final CommandGateway instance, final CreationalContext<CommandGateway> creationalContext) {
			creationalContext.release();
		}

	}

}
