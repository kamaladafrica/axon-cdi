package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.util.Objects;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public abstract class AbstractBeanCreation implements BeanCreation {

	private final BeanCreation original;

	public AbstractBeanCreation(final BeanCreation original) {
		this.original = Objects.requireNonNull(original);
	}

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
				final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo,
				final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		original.create(afterBeanDiscovery, beanManager, aggregateRootBeanInfo, configuration);
		Bean<?> newBeanToAdd = concreateCreateBean(beanManager, aggregateRootBeanInfo, configuration);
		if (newBeanToAdd != null) {
			afterBeanDiscovery.addBean(newBeanToAdd);
		}
	}

	protected abstract Bean<?> concreateCreateBean(BeanManager beanManager, AggregateRootBeanInfo aggregateRootBeanInfo, Configuration configuration);

}
