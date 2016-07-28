package org.axonframework.integration.cdi.extension.impl;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public abstract class AxonConfigInfo {

	private final Class<?> type;

	private final Optional<Bean<?>> bean;

	private AxonConfigInfo(Class<?> type, Bean<?> bean) {
		this.type = Objects.requireNonNull(type);
		this.bean = Optional.<Bean<?>> fromNullable(bean);
	}

	protected AxonConfigInfo(Class<?> type) {
		this(type, null);
	}

	protected AxonConfigInfo(Bean<?> bean) {
		this(bean.getBeanClass(), bean);
	}

	public final Class<?> getType() {
		return type;
	}

	public final Optional<Bean<?>> getBean() {
		return bean;
	}

	public final AnnotatedType<?> getAnnotatedType(BeanManager bm) {
		return bm.createAnnotatedType(type);
	}

	protected final Bean<?> resolveBean(final BeanManager bm, final Set<Annotation> qualifiers) {
		Supplier<Bean<?>> beanSupplier = new Supplier<Bean<?>>() {

			@Override
			public Bean<?> get() {
				return bm.resolve(getBeans(bm, qualifiers));
			}
		};
		return getBean().or(beanSupplier);
	}

	protected final Set<Bean<?>> getBeans(final BeanManager bm,
			final Set<Annotation> qualifierSet) {
		final Annotation[] qualifiers = Iterables.toArray(qualifierSet, Annotation.class);
		return bm.getBeans(type, qualifiers);
	}

	protected final Object getReference(BeanManager bm, Set<Annotation> qualifiers) {
		Bean<?> bean = resolveBean(bm, qualifiers);
		return bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(null));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AxonConfigInfo other = (AxonConfigInfo) obj;
		if (bean == null) {
			if (other.bean != null) {
				return false;
			}
		} else if (!bean.equals(other.bean)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
