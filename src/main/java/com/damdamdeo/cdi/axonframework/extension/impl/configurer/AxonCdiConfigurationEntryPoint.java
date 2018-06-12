package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class AxonCdiConfigurationEntryPoint implements AxonCdiConfigurer {

	@Override
	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		return configurer;
	}

}
