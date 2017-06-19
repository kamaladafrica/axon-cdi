package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class AggregatesCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<AggregateRootBeanInfo> aggregateRootBeanInfos;

	public AggregatesCdiConfigurer(final AxonCdiConfigurer original, final Set<AggregateRootBeanInfo> aggregateBeanInfos) {
		super(original);
		this.aggregateRootBeanInfos = Objects.requireNonNull(aggregateBeanInfos);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		for (AggregateRootBeanInfo aggregateRootBeanInfo : aggregateRootBeanInfos) {
//			Type type = TypeUtils.parameterize(Repository.class,
//					aggregateRootBeanInfo.type());
			AggregateConfigurer aggregateConf =
                    AggregateConfigurer.defaultConfiguration(aggregateRootBeanInfo.type());
//			aggregateConf.configureAggregateFactory(aggregateFactoryBuilder);
//			aggregateConf.configureCommandHandler(aggregateCommandHandlerBuilder)
			configurer.configureAggregate(aggregateConf);
		}
	}

}
