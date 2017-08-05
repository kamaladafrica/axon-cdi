package it.kamaladafrica.cdi.axonframework.extension.impl.bean;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// cf.SimpleEventSchedulerFactoryBean
// not used in the DefaultConfigurer
public class EventSchedulerBeanCreation extends AbstractBeansCreationHandler {

	public EventSchedulerBeanCreation(final BeansCreationHandler original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final ExecutionContext executionContext,
				final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		if (!executionContext.hasAnEventSchedulerBean(beanManager)) {
			BeanBuilder<EventScheduler> builder = new BeanBuilder<EventScheduler>(beanManager)
				.beanClass(EventScheduler.class)
				.qualifiers(executionContext.eventSchedulerQualifiers())
				.types(EventScheduler.class)
				.beanLifecycle(
					new SimpleEventSchedulerContextualLifecycle(configuration));
			Bean<?> newEventSchedulerBeanToAdd = builder.create();
			return Collections.singleton(newEventSchedulerBeanToAdd);
		}
		return Collections.<Bean<?>> emptySet();
	}

	private class SimpleEventSchedulerContextualLifecycle implements ContextualLifecycle<EventScheduler> {

		private final Configuration configuration;

		public SimpleEventSchedulerContextualLifecycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		public EventScheduler create(final Bean<EventScheduler> bean, final CreationalContext<EventScheduler> creationalContext) {
			// remember it is a proxy... just a technical detail :)
			return (EventScheduler) new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(),
					configuration.eventBus());
		}

		@Override
		public void destroy(final Bean<EventScheduler> bean, final EventScheduler instance, final CreationalContext<EventScheduler> creationalContext) {
			creationalContext.release();
		}

	}

}
