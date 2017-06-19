package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

// cf. DefaultConfigurerTest: EntityManagerTransactionManager
public class TransactionManagerCdiConfigurer extends AbstractCdiConfiguration {

	public TransactionManagerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Bean<?> bean = CdiUtils.getBean(beanManager, TransactionManager.class, normalizedQualifiers);
		if (bean != null) {
			TransactionManager transactionManager = (TransactionManager) Proxy.newProxyInstance(
					TransactionManager.class.getClassLoader(),
					new Class[] { TransactionManager.class },
					new TransactionManagerInvocationHandler(beanManager, normalizedQualifiers));
			configurer.configureTransactionManager(c -> transactionManager);
		}
	}

	private class TransactionManagerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private TransactionManager transactionManager;

		public TransactionManagerInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (transactionManager == null) {
				transactionManager = (TransactionManager) CdiUtils.getReference(beanManager, TransactionManager.class, qualifiers);
			}
			return method.invoke(transactionManager, args);
		}

	}

}
