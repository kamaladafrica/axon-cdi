package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;
import java.util.function.Function;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.AnnotationCommandTargetResolver;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.aggregate.CustomAggregateAnnotationCommandHandler;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// order of call is very important
public class AggregatesCdiConfigurer extends AbstractCdiConfiguration {

	public AggregatesCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		
		for (AggregateRootBeanInfo aggregateRootBeanInfo : executionContext.aggregateRootBeanInfos()) {
			AggregateConfigurerUsingProxies aggregateConf = AggregateConfigurerUsingProxies.defaultConfiguration(aggregateRootBeanInfo.type());
			aggregateConf.configureCommandHandler(new Function<Configuration, AggregateAnnotationCommandHandler<?>>() {

				@Override
				public AggregateAnnotationCommandHandler<?> apply(final Configuration configuration) {
					return new CustomAggregateAnnotationCommandHandler<>(aggregateConf.aggregateType(),
							aggregateConf.repository(),
							new AnnotationCommandTargetResolver(),
							configuration.parameterResolverFactory());
				}

			});
			configurer.configureAggregate(aggregateConf);
		}
	}

}
