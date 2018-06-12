package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

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
			Class<? extends TokenStore> proxyTokenStore = new ByteBuddy()
				.subclass(TokenStore.class)
				.method(ElementMatchers.any())
				.intercept(InvocationHandlerAdapter.of(new TokenStoreInvocationHandler(beanManager, executionContext)))
				.make()
				.load(TokenStore.class.getClassLoader())
				.getLoaded();
			TokenStore instanceTokenStore = proxyTokenStore.newInstance();
			configurer.registerComponent(TokenStore.class, c -> instanceTokenStore);
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
