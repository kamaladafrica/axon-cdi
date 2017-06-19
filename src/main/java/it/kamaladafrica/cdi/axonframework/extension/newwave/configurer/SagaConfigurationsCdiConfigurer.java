package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.axonframework.config.Configurer;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.saga.repository.SagaStore;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.SagaBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class SagaConfigurationsCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<SagaBeanInfo> sagaBeanInfos;

	public SagaConfigurationsCdiConfigurer(final AxonCdiConfigurer original, final Set<SagaBeanInfo> sagaBeanInfos) {
		super(original);
		this.sagaBeanInfos = Objects.requireNonNull(sagaBeanInfos);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		sagaBeanInfos.stream().filter(new Predicate<SagaBeanInfo>() {

			public boolean test(final SagaBeanInfo sagaInfo) {
				return CdiUtils.qualifiersMatch(sagaInfo.normalizedQualifiers(), normalizedQualifiers);
			};

		}).forEach(sagaBeanInfo -> {
			Type type = TypeUtils.parameterize(SagaStore.class,
					sagaBeanInfo.type());
			SagaConfiguration sagaConfiguration = SagaConfiguration.subscribingSagaManager(sagaBeanInfo.type());
			Bean<?> bean = CdiUtils.getBean(beanManager, type, normalizedQualifiers);
			if (bean != null) {
				sagaConfiguration.configureSagaStore(c -> (SagaStore) Proxy.newProxyInstance(
						SagaStore.class.getClassLoader(),
						new Class[] { SagaStore.class },
						new SagaStoreInvocationHandler(beanManager, normalizedQualifiers, type)));
			}
			configurer.registerModule(sagaConfiguration);
		});
	}

	private class SagaStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private final Type type;
		private SagaStore<?> sagaStore;

		public SagaStoreInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers, final Type type) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
			this.type = Objects.requireNonNull(type);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (sagaStore == null) {
				sagaStore = (SagaStore<?>) CdiUtils.getReference(beanManager, type, qualifiers);
			}
			return method.invoke(sagaStore, args);
		}

	}

}
