package com.damdamdeo.cdi.axonframework.extension.impl.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.Snapshotter;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// code coming from SpringAggregateSnapshotterFactoryBean and SpringAggregateSnapshotter
public class SnapshotterBeanCreation extends AbstractBeansCreationHandler {

	// create AggregateSnapshotter bean
	// this bean is used by SnapshotterTriggerDefinitionCdiConfigurer
	// I can't used a producer because It would be too complicated to create it (ie I need the list of AggregateRepository)

	public SnapshotterBeanCreation(final BeansCreationHandler original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final ExecutionContext executionContext,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		BeanBuilder<Snapshotter> builder = new BeanBuilder<Snapshotter>(beanManager)
			.beanClass(Snapshotter.class)
			.qualifiers(executionContext.snapshotterTriggerDefinitionQualifiers())
			.types(Snapshotter.class)
			.beanLifecycle(
				new AggregateSnapshotterContextualLifecycle(executionContext, configuration));
		Bean<?> snapshotterBean = builder.create();
		return Collections.singleton(snapshotterBean);
	}

	private class AggregateSnapshotterContextualLifecycle implements ContextualLifecycle<Snapshotter> {

		private final ExecutionContext executionContext;
		private final Configuration configuration;

		public AggregateSnapshotterContextualLifecycle(final ExecutionContext executionContext, final Configuration configuration) {
			this.executionContext = Objects.requireNonNull(executionContext);
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		public Snapshotter create(final Bean<Snapshotter> bean, final CreationalContext<Snapshotter> creationalContext) {
			// putain, pourquoi dans la conf il n'expose pas les differents repository disponibles ... ce serait tellement plus simple...
			List<AggregateFactory<?>> aggregateFactories = executionContext.aggregateRootBeanInfos()
				.stream()
				.map(new Function<AggregateRootBeanInfo, AggregateFactory<?>>() {

					@Override
					public AggregateFactory<?> apply(final AggregateRootBeanInfo aggregateRootBeanInfo) {
						Repository<?> repository = configuration.repository(aggregateRootBeanInfo.type());
						return ((EventSourcingRepository<?>) repository).getAggregateFactory();
					}

				}).collect(Collectors.toList());
			return (Snapshotter) new AggregateSnapshotter(configuration.eventStore(), aggregateFactories);
		}

		@Override
		public void destroy(final Bean<Snapshotter> bean, final Snapshotter instance, final CreationalContext<Snapshotter> creationalContext) {
			creationalContext.release();
		}

	}

}
