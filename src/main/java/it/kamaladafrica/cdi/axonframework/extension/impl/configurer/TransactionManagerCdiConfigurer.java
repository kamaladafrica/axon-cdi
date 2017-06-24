package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// cf. DefaultConfigurerTest: EntityManagerTransactionManager
public class TransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public TransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (!executionContext.hasATransactionManagerBean(beanManager)) {
			TransactionManager transactionManager = (TransactionManager) Proxy.newProxyInstance(
				TransactionManager.class.getClassLoader(),
				new Class[] { TransactionManager.class },
				new TransactionManagerInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.configureTransactionManager(c -> transactionManager);
		}
	}

	private class TransactionManagerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private TransactionManager transactionManager;

		public TransactionManagerInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (transactionManager == null) {
				transactionManager = executionContext.getTransactionManagerReference(beanManager);
			}
			return method.invoke(transactionManager, args);
		}

	}

}
