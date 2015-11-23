package org.axonframework.integration.cdi.extension;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.integration.cdi.CdiResourceInjector;
import org.axonframework.saga.GenericSagaFactory;
import org.axonframework.saga.SagaFactory;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.AnnotatedSagaManager;

public class SagaManagerContextualLifecycle<T extends AbstractAnnotatedSaga> implements
		ContextualLifecycle<AnnotatedSagaManager> {

	private final Collection<AnnotatedType<T>> sagaTypes;

	public SagaManagerContextualLifecycle(
			Collection<AnnotatedType<T>> sagaTypes) {
		this.sagaTypes = sagaTypes;
	}

	@Override
	public AnnotatedSagaManager create(Bean<AnnotatedSagaManager> bean,
			CreationalContext<AnnotatedSagaManager> creationalContext) {
		Annotation[] qualifiers = bean.getQualifiers().toArray(
				new Annotation[bean.getQualifiers().size()]);
		Instance<Object> instances = CDI.current();
		SagaRepository repository = instances.select(SagaRepository.class, qualifiers).get();
		Instance<SagaFactory> factories = instances.select(SagaFactory.class, qualifiers);
		if (factories.isUnsatisfied()) {
			factories = instances.select(SagaFactory.class);
		}
		SagaFactory factory;
		if (factories.isUnsatisfied()) {
			GenericSagaFactory gsf = new GenericSagaFactory();
			gsf.setResourceInjector(new CdiResourceInjector());
			factory = gsf;
		} else {
			factory = factories.get();
		}

		@SuppressWarnings("unchecked")
		Class<T>[] sagaClasses = new Class[sagaTypes.size()];

		int i = 0;
		for (AnnotatedType<T> sagaType : sagaTypes) {
			sagaClasses[i] = sagaType.getJavaClass();
			i++;
		}
		return new AnnotatedSagaManager(repository, factory, sagaClasses);
	}

	@Override
	public void destroy(Bean<AnnotatedSagaManager> bean, AnnotatedSagaManager instance,
			CreationalContext<AnnotatedSagaManager> creationalContext) {
		creationalContext.release();
	}

}
