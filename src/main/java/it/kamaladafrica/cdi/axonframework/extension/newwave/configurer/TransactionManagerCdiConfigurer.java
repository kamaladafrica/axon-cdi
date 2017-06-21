package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

// cf. DefaultConfigurerTest: EntityManagerTransactionManager
public class TransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public TransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.TRANSACTION_MANAGER);
		if (bean != null) {
			TransactionManager transactionManager = (TransactionManager) Proxy.newProxyInstance(
					TransactionManager.class.getClassLoader(),
					new Class[] { TransactionManager.class },
					new TransactionManagerInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.configureTransactionManager(c -> transactionManager);
		}
	}

	private class TransactionManagerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private TransactionManager transactionManager;

		public TransactionManagerInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (transactionManager == null) {
				transactionManager = (TransactionManager) aggregateRootBeanInfo.getReference(beanManager, QualifierType.TRANSACTION_MANAGER);
			}
			return method.invoke(transactionManager, args);
		}

	}

}
