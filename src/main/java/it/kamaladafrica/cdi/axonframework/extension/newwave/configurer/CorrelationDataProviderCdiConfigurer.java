package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class CorrelationDataProviderCdiConfigurer extends AbstractCdiConfiguration {

	public CorrelationDataProviderCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);

		Set<Bean<?>> beansCorrelationProvider = aggregateRootBeanInfo.getBeans(beanManager, QualifierType.CORRELATION_DATA_PROVIDER);
		List<CorrelationDataProvider> correlationDataProviders = beansCorrelationProvider.stream()
				.map(new Function<Bean<?>, CorrelationDataProvider>() {

					@Override
					public CorrelationDataProvider apply(final Bean<?> bean) {
						// que dois je faire de mon objet bean ???
						return (CorrelationDataProvider) Proxy.newProxyInstance(
								CorrelationDataProvider.class.getClassLoader(),
								new Class[] { CorrelationDataProvider.class },
								new CorrelationDataProviderInvocationHandler(beanManager, bean, aggregateRootBeanInfo));
					}

				}).collect(Collectors.toList());

		if (!correlationDataProviders.isEmpty()) {
			configurer.configureCorrelationDataProviders(c -> correlationDataProviders);
		}
	}

	private class CorrelationDataProviderInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Bean<?> bean;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private CorrelationDataProvider correlationDataProvider;

		public CorrelationDataProviderInvocationHandler(final BeanManager beanManager, final Bean<?> bean, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.bean = Objects.requireNonNull(bean);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (correlationDataProvider == null) {
				correlationDataProvider = (CorrelationDataProvider) aggregateRootBeanInfo.getReference(beanManager, bean);
			}
			return method.invoke(correlationDataProvider, args);
		}

	}

}
