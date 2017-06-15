package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.apache.deltaspike.core.util.BeanUtils;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractAutoConfiguringProducer<X> implements Producer<X> {

	private final Producer<X> wrappedProducer;

	private final AnnotatedMember<?> annotatedMember;

	private final BeanManager beanManager;

	public AbstractAutoConfiguringProducer(final Producer<X> wrappedProducer,
			final AnnotatedMember<?> annotatedMember,
			final BeanManager beanManager) {
		this.wrappedProducer = wrappedProducer;
		this.beanManager = beanManager;
		this.annotatedMember = annotatedMember;
	}

	@Override
	public X produce(final CreationalContext<X> ctx) {
		X instance = wrappedProducer.produce(ctx);
		return configure(instance);
	}

	protected abstract X configure(X instance);

	@Override
	public void dispose(final X instance) {
		wrappedProducer.dispose(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return wrappedProducer.getInjectionPoints();
	}

	protected final Producer<X> getWrappedProducer() {
		return wrappedProducer;
	}

	protected final AnnotatedMember<?> getAnnotatedMember() {
		return annotatedMember;
	}

	protected final BeanManager getBeanManager() {
		return beanManager;
	}

	protected Annotation[] getQualifiers() {
		Set<Annotation> qualifiers = CdiUtils.normalizedQualifiers(
				BeanUtils.getQualifiers(beanManager, annotatedMember.getAnnotations()));
		return qualifiers.toArray(new Annotation[qualifiers.size()]);
	}

}
