package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.saga.repository.SagaStore;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.SagaBeanInfo;

public class SagaConfigurationsCdiConfigurer extends AbstractCdiConfiguration {

	private final Set<SagaBeanInfo> sagaBeanInfos;

	public SagaConfigurationsCdiConfigurer(final AxonCdiConfigurer original, final Set<SagaBeanInfo> sagaBeanInfos) {
		super(original);
		this.sagaBeanInfos = Objects.requireNonNull(sagaBeanInfos);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		sagaBeanInfos.stream().filter(new Predicate<SagaBeanInfo>() {

			public boolean test(final SagaBeanInfo sagaBeanInfo) {
				return aggregateRootBeanInfo.qualifiers(AggregateRootBeanInfo.QualifierType.EVENT_BUS)
						.equals(sagaBeanInfo.qualifiers(SagaBeanInfo.QualifierType.EVENT_BUS));
			};

		}).forEach(sagaBeanInfo -> {
			SagaConfiguration sagaConfiguration = SagaConfiguration.subscribingSagaManager(sagaBeanInfo.type());
			Bean<?> bean = sagaBeanInfo.resolveSagaStoreBean(beanManager);
			if (bean != null) {
				sagaConfiguration.configureSagaStore(c -> (SagaStore) Proxy.newProxyInstance(
						SagaStore.class.getClassLoader(),
						new Class[] { SagaStore.class },
						new SagaStoreInvocationHandler(beanManager, sagaBeanInfo, bean)));
				SagaConfiguration.trackingSagaManager(sagaBeanInfo.type());
			}
			configurer.registerModule(sagaConfiguration);
		});
	}

	private class SagaStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final SagaBeanInfo sagaBeanInfo;
		private final Bean<?> sagaBean;
		private SagaStore<?> sagaStore;

		public SagaStoreInvocationHandler(final BeanManager beanManager, final SagaBeanInfo sagaBeanInfo, final Bean<?> sagaBean) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.sagaBeanInfo = Objects.requireNonNull(sagaBeanInfo);
			this.sagaBean = Objects.requireNonNull(sagaBean);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (sagaStore == null) {
				sagaStore = (SagaStore<?>) sagaBeanInfo.getReference(beanManager, sagaBean);
			}
			return method.invoke(sagaStore, args);
		}

	}

}
