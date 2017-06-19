package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
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

// cf.SimpleEventSchedulerFactoryBean
// not used in the DefaultConfigurer
public class EventSchedulerBeanCreation extends AbstractBeansCreation {

	public EventSchedulerBeanCreation(final BeansCreation original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers,
				final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Objects.requireNonNull(configuration);
		
		BeanBuilder<EventScheduler> builder = new BeanBuilder<EventScheduler>(beanManager)
			.beanClass(EventScheduler.class)
			.qualifiers(normalizedQualifiers)
			.types(EventScheduler.class)
			.beanLifecycle(
				new SimpleEventSchedulerContextualLifecycle<EventScheduler>(configuration));
		Bean<?> eventSchedulerBean = builder.create();
		return Collections.singleton(eventSchedulerBean);
	}

	private class SimpleEventSchedulerContextualLifecycle<T extends EventScheduler> implements ContextualLifecycle<T> {

		private final Configuration configuration;

		public SimpleEventSchedulerContextualLifecycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
			// remember it is a proxy... just a technical detail :)
			return (T) new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(),
					configuration.eventBus());
		}

		@Override
		public void destroy(final Bean<T> bean, final T instance, final CreationalContext<T> creationalContext) {
			creationalContext.release();
		}

	}

}
