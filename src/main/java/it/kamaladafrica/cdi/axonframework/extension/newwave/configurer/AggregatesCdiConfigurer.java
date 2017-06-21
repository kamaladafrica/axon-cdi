package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class AggregatesCdiConfigurer extends AbstractCdiConfiguration {

	public AggregatesCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		
//		Type type = TypeUtils.parameterize(Repository.class,
//				aggregateRootBeanInfo.type());
		AggregateConfigurer aggregateConf = AggregateConfigurer.defaultConfiguration(aggregateRootBeanInfo.type());
//		aggregateConf.configureAggregateFactory(aggregateFactoryBuilder);
//		aggregateConf.configureCommandHandler(aggregateCommandHandlerBuilder)
		configurer.configureAggregate(aggregateConf);
	}

}
