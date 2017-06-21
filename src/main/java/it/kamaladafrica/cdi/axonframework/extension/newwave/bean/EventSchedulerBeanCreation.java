package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.util.Objects;
import java.util.concurrent.Executors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

// cf.SimpleEventSchedulerFactoryBean
// not used in the DefaultConfigurer
public class EventSchedulerBeanCreation extends AbstractBeanCreation {

	public EventSchedulerBeanCreation(final BeanCreation original) {
		super(original);
	}

	@Override
	protected Bean<?> concreateCreateBean(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo,
				final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Objects.requireNonNull(configuration);
		
		BeanBuilder<EventScheduler> builder = new BeanBuilder<EventScheduler>(beanManager)
			.beanClass(EventScheduler.class)
			.qualifiers(aggregateRootBeanInfo.qualifiers(QualifierType.EVENT_SCHEDULER))
			.types(EventScheduler.class)
			.beanLifecycle(
				new SimpleEventSchedulerContextualLifecycle<EventScheduler>(configuration));
		Bean<?> eventSchedulerBean = builder.create();
		return eventSchedulerBean;
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
