package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.Validate;
import org.axonframework.config.Configurer;
import org.axonframework.metrics.GlobalMetricRegistry;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class MetricRegistryCdiConfigurer extends AbstractCdiConfiguration {

	public MetricRegistryCdiConfigurer(AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext)
			throws Exception {
		Validate.notNull(configurer);
		Validate.notNull(beanManager);
		Validate.notNull(executionContext);
		// I can't inject a MetricRegistry because I can't have a CDI reference yet... and GlobalMetricRegistry need the reference
		// So I will produce a "default" bean using the configuration
		GlobalMetricRegistry globalMetricRegistry = new GlobalMetricRegistry(executionContext.metricRegistry());
		globalMetricRegistry.registerWithConfigurer(configurer);
	}

}
