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
import org.axonframework.config.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class MetricRegistryBeanCreation extends AbstractBeansCreationHandler {

	public MetricRegistryBeanCreation(final BeansCreationHandler original) {
		super(original);
	}

	@Override
	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final ExecutionContext executionContext,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(configuration);
		BeanBuilder<MetricRegistry> builder = new BeanBuilder<MetricRegistry>(beanManager)
				.beanClass(MetricRegistry.class)
				.qualifiers(executionContext.commandGatewayQualifiers())
				.types(MetricRegistry.class)
				.scope(ApplicationScoped.class)
				.beanLifecycle(new MetricRegistryContextualLifeCycle(executionContext));
		Bean<?> newMetricRegistryBeanToAdd = builder.create();
		return Collections.singleton(newMetricRegistryBeanToAdd);
	}

	private class MetricRegistryContextualLifeCycle implements ContextualLifecycle<MetricRegistry> {

		private final ExecutionContext executionContext;

		public MetricRegistryContextualLifeCycle(final ExecutionContext executionContext) {
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public MetricRegistry create(final Bean<MetricRegistry> bean, final CreationalContext<MetricRegistry> creationalContext) {
			return executionContext.metricRegistry();
		}

		@Override
		public void destroy(final Bean<MetricRegistry> bean, final MetricRegistry instance,
				final CreationalContext<MetricRegistry> creationalContext) {
			creationalContext.release();
		}

	}

}
