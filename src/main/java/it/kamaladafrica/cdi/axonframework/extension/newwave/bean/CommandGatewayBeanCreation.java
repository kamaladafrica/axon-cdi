package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
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

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class CommandGatewayBeanCreation extends AbstractBeansCreation {

	public CommandGatewayBeanCreation(final BeansCreation original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Objects.requireNonNull(configuration);
		if (CdiUtils.getBean(beanManager, CommandGateway.class, normalizedQualifiers) == null) {
			// No CommandGateway has been Produces so create a default one from *configuration* ^^
			// It can be injected in facade next :)
			BeanBuilder<CommandGateway> builder = new BeanBuilder<CommandGateway>(beanManager)
				.beanClass(CommandGateway.class)
				.qualifiers(normalizedQualifiers)
				.types(CommandGateway.class)
				.scope(ApplicationScoped.class)
				.beanLifecycle(
					new CommandGatewayContextualLifeCycle<CommandGateway>(configuration));
			Bean<?> commandGatewayBean = builder.create();
			return Collections.singleton(commandGatewayBean);
		}
		return Collections.<Bean<?>> emptySet();
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
