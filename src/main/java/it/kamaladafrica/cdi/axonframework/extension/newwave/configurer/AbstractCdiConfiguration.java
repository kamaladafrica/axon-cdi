package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;

public abstract class AbstractCdiConfiguration implements AxonCdiConfigurer {

	private final AxonCdiConfigurer original;

	public AbstractCdiConfiguration(final AxonCdiConfigurer original) {
		this.original = Objects.requireNonNull(original);
	}

	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Configurer originalConfigurer = original.setUp(configurer, beanManager, aggregateRootBeanInfo);
		concreateCdiSetUp(originalConfigurer, beanManager, aggregateRootBeanInfo);
		return originalConfigurer;
	};

	protected abstract void concreateCdiSetUp(Configurer configurer, BeanManager beanManager, AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception;

}
