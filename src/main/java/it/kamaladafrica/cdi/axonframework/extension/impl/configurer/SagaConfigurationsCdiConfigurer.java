package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.saga.repository.SagaStore;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.SagaBeanInfo;

public class SagaConfigurationsCdiConfigurer extends AbstractCdiConfiguration {

	public SagaConfigurationsCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		executionContext.sagaBeanInfos()
			.stream()
			.forEach(sagaBeanInfo -> {
				// can have multiple
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
