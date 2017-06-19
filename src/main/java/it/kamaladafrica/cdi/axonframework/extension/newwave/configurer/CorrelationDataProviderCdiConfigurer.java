package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CorrelationDataProvidedInfo;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class CorrelationDataProviderCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<CorrelationDataProvidedInfo> correlationDataProvidedInfos;

	public CorrelationDataProviderCdiConfigurer(final AxonCdiConfigurer original, final Set<CorrelationDataProvidedInfo> correlationDataProviderInfos) {
		super(original);
		this.correlationDataProvidedInfos = correlationDataProviderInfos;
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		List<CorrelationDataProvider> correlationDataProviders = new ArrayList<>();
		for (CorrelationDataProvidedInfo correlationDataProvidedInfo : correlationDataProvidedInfos) {
			if (CdiUtils.qualifiersMatch(correlationDataProvidedInfo.normalizedQualifiers(), normalizedQualifiers)) {
				correlationDataProviders.addAll(CdiUtils.getBeans(beanManager, CorrelationDataProvider.class, normalizedQualifiers)
						.stream()
						.map(new Function<Bean<?>, CorrelationDataProvider>() {

							@Override
							public CorrelationDataProvider apply(final Bean<?> bean) {
								return (CorrelationDataProvider) Proxy.newProxyInstance(
										CorrelationDataProvider.class.getClassLoader(),
										new Class[] { CorrelationDataProvider.class },
										new CorrelationDataProviderInvocationHandler(beanManager, normalizedQualifiers));
							}

						}).collect(Collectors.toList()));
			}
		}
		if (!correlationDataProviders.isEmpty()) {
			configurer.configureCorrelationDataProviders(c -> correlationDataProviders);
		}
	}

	private class CorrelationDataProviderInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private CorrelationDataProvider correlationDataProvider;

		public CorrelationDataProviderInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (correlationDataProvider == null) {
				correlationDataProvider = (CorrelationDataProvider) CdiUtils.getReference(beanManager, CorrelationDataProvider.class, qualifiers);
			}
			return method.invoke(correlationDataProvider, args);
		}

	}

}
