package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import com.google.common.base.Preconditions;

public class HandlerInfo extends AxonConfigInfo {

	private final Set<Annotation> qualifiers;

	private final boolean eventHandler;

	private HandlerInfo(final Class<?> type, final Set<Annotation> qualifiers, final boolean eventHandler) {
		super(type);
		ensureHandlerType(eventHandler, type);
		this.qualifiers = Collections.unmodifiableSet(CdiUtils.normalizedQualifiers(qualifiers));
		this.eventHandler = eventHandler;
	}

	private HandlerInfo(final Bean<?> bean, final Set<Annotation> qualifiers, final boolean eventHandler) {
		super(bean);
		ensureHandlerType(eventHandler, bean.getBeanClass());
		this.qualifiers = Collections.unmodifiableSet(CdiUtils.normalizedQualifiers(qualifiers));
		this.eventHandler = eventHandler;
	}

	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	public boolean isEventHandler() {
		return eventHandler;
	}

	public boolean isCommandHandler() {
		return !eventHandler;
	}

	public Set<Bean<?>> getBeans(final BeanManager bm) {
		return super.getBeans(bm, getQualifiers());
	}

	public Bean<?> resolveBean(final BeanManager bm) {
		return super.resolveBean(bm, getQualifiers());
	}

	public Object getReference(final BeanManager bm) {
		return super.getReference(bm, getQualifiers());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (eventHandler ? 1231 : 1237);
		result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HandlerInfo other = (HandlerInfo) obj;
		if (eventHandler != other.eventHandler) {
			return false;
		}
		if (qualifiers == null) {
			if (other.qualifiers != null) {
				return false;
			}
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		}
		return true;
	}

	public static HandlerInfo eventHandler(final BeanManager bm, final AnnotatedType<?> annotated) {
		return new HandlerInfo(annotated.getJavaClass(), CdiUtils.qualifiers(bm, annotated), true);
	}

	public static HandlerInfo commandHandler(final BeanManager bm, final AnnotatedType<?> annotated) {
		return new HandlerInfo(annotated.getJavaClass(), CdiUtils.qualifiers(bm, annotated), false);
	}

	public static HandlerInfo eventHandler(final Bean<?> bean) {
		return new HandlerInfo(bean, bean.getQualifiers(), true);
	}

	public static HandlerInfo commandHandler(final Bean<?> bean) {
		return new HandlerInfo(bean, bean.getQualifiers(), false);
	}

	private static void ensureHandlerType(final boolean eventHandler, final Class<?> handlerClass) {
		if (eventHandler) {
			Preconditions.checkArgument(AxonUtils.isEventHandler(handlerClass),
					"Provided type is not a valid event handler: " + handlerClass.getName());
		} else {
			Preconditions.checkArgument(AxonUtils.isCommandHandler(handlerClass),
					"Provided type is not a valid command handler: " + handlerClass.getName());
		}
	}

}
