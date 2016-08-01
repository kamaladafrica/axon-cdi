package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.support.CdiParameterResolverFactory;
import it.kamaladafrica.cdi.axonframework.support.CdiSagaFactory;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.saga.SagaFactory;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.AnnotatedSagaManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class SagaManagerContextualLifecycle<T extends AbstractAnnotatedSaga> implements
		ContextualLifecycle<AnnotatedSagaManager> {

	private final BeanManager beanManager;

	private final SagaManagerInfo sagaManager;

	public SagaManagerContextualLifecycle(BeanManager beanManager, SagaManagerInfo sagaManager) {
		this.beanManager = Objects.requireNonNull(beanManager);
		this.sagaManager = Objects.requireNonNull(sagaManager);

	}

	@Override
	@SuppressWarnings("unchecked")
	public AnnotatedSagaManager create(Bean<AnnotatedSagaManager> bean,
			CreationalContext<AnnotatedSagaManager> creationalContext) {
		Iterable<Class<? extends AbstractAnnotatedSaga>> sagas = Iterables
				.transform(sagaManager.getSagas(), ToSagaClass.INSTANCE);
		return new AnnotatedSagaManager(repository(), factory(),
				new CdiParameterResolverFactory(beanManager),
				Iterables.toArray(sagas, Class.class));
	}

	@Override
	public void destroy(Bean<AnnotatedSagaManager> bean, AnnotatedSagaManager instance,
			CreationalContext<AnnotatedSagaManager> creationalContext) {
		creationalContext.release();
	}

	private SagaRepository repository() {
		return (SagaRepository) CdiUtils.getReference(beanManager, SagaRepository.class,
				sagaManager.getRepositoryQualifiers());
	}

	private SagaFactory factory() {
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, SagaFactory.class,
				sagaManager.getFactoryQualifiers());
		Bean<?> bean = beanManager.resolve(beans);
		return bean == null ? new CdiSagaFactory(beanManager)
				: (SagaFactory) CdiUtils.getReference(beanManager, bean, SagaFactory.class);
	}

	private static class ToSagaClass
			implements Function<SagaInfo, Class<? extends AbstractAnnotatedSaga>> {

		public static final Function<SagaInfo, Class<? extends AbstractAnnotatedSaga>> INSTANCE = new ToSagaClass();

		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends AbstractAnnotatedSaga> apply(SagaInfo input) {
			return (Class<? extends AbstractAnnotatedSaga>) input.getType();
		}

	}

}
