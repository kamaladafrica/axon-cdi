package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

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
		
//		Type type = TypeUtils.parameterize(Repository.class,
//				aggregateRootBeanInfo.type());
		for (AggregateRootBeanInfo aggregateRootBeanInfo : executionContext.aggregateRootBeanInfos()) {
			AggregateConfigurer aggregateConf = AggregateConfigurer.defaultConfiguration(aggregateRootBeanInfo.type());
//		aggregateConf.configureAggregateFactory(aggregateFactoryBuilder);
//		aggregateConf.configureCommandHandler(aggregateCommandHandlerBuilder)
			configurer.configureAggregate(aggregateConf);
		}
	}

}
