package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class TokenStoreCdiConfigurer extends AbstractCdiConfiguration {

	public TokenStoreCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(normalizedQualifiers);
		Bean<?> bean = CdiUtils.getBean(beanManager, TokenStore.class, normalizedQualifiers);
		if (bean != null) {
			TokenStore tokenStore = (TokenStore) Proxy.newProxyInstance(
					TokenStore.class.getClassLoader(),
					new Class[] { TokenStore.class },
					new TokenStoreInvocationHandler(beanManager, normalizedQualifiers));
			configurer.registerComponent(TokenStore.class, c -> tokenStore);
		}
	}

	private class TokenStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final Set<Annotation> qualifiers;
		private TokenStore tokenStore;

		public TokenStoreInvocationHandler(final BeanManager beanManager, final Set<Annotation> normalizedQualifiers) {
			this.beanManager = Objects.requireNonNull(beanManager);
			Objects.requireNonNull(normalizedQualifiers);
			this.qualifiers = ImmutableSet.copyOf(normalizedQualifiers);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (tokenStore == null) {
				tokenStore = (TokenStore) CdiUtils.getReference(beanManager, TokenStore.class, qualifiers);
			}
			return method.invoke(tokenStore, args);
		}

	}

}
