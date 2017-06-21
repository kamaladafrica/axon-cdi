package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

public class TokenStoreCdiConfigurer extends AbstractCdiConfiguration {

	public TokenStoreCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.TOKEN_STORE);
		if (bean != null) {
			TokenStore tokenStore = (TokenStore) Proxy.newProxyInstance(
					TokenStore.class.getClassLoader(),
					new Class[] { TokenStore.class },
					new TokenStoreInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.registerComponent(TokenStore.class, c -> tokenStore);
		}
	}

	private class TokenStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private TokenStore tokenStore;

		public TokenStoreInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (tokenStore == null) {
				tokenStore = (TokenStore) aggregateRootBeanInfo.getReference(beanManager, QualifierType.TOKEN_STORE);
			}
			return method.invoke(tokenStore, args);
		}

	}

}
