package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.util.Objects;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public class BeansCreationEntryPoint implements BeanCreation {

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
			final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo, final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Objects.requireNonNull(configuration);
		// do nothing : just the entry point
	}

}
