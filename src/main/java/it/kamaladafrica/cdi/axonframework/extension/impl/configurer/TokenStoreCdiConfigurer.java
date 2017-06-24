package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class TokenStoreCdiConfigurer extends AbstractCdiConfiguration {

	public TokenStoreCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (executionContext.hasATokenStoreBean(beanManager)) {
			TokenStore tokenStore = (TokenStore) Proxy.newProxyInstance(
				TokenStore.class.getClassLoader(),
				new Class[] { TokenStore.class },
				new TokenStoreInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.registerComponent(TokenStore.class, c -> tokenStore);
		}
	}

	private class TokenStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private TokenStore tokenStore;

		public TokenStoreInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (tokenStore == null) {
				tokenStore = executionContext.getTokenStoreReference(beanManager);
			}
			return method.invoke(tokenStore, args);
		}

	}

}
