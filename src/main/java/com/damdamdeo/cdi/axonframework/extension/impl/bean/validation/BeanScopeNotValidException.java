package com.damdamdeo.cdi.axonframework.extension.impl.bean.validation;

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;

public class BeanScopeNotValidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8655849384412988834L;

	private final Bean<?> bean;

	private final Class<? extends Annotation> expectedBeanScoped;

	private final Class<? extends Annotation> currentBeanScoped;

	public BeanScopeNotValidException(final Bean<?> bean, final Class<? extends Annotation> expectedBeanScoped, final Class<? extends Annotation> currentBeanScoped) {
		this.bean = Objects.requireNonNull(bean);
		this.expectedBeanScoped = Objects.requireNonNull(expectedBeanScoped);
		this.currentBeanScoped = Objects.requireNonNull(currentBeanScoped);
		if (expectedBeanScoped.equals(currentBeanScoped)) {
			throw new IllegalStateException("Expected and current scope must be differents");
		}
	}

	public Bean<?> bean() {
		return bean;
	}

	public Class<? extends Annotation> expectedBeanScoped() {
		return expectedBeanScoped;
	}

	public Class<? extends Annotation> currentBeanScoped() {
		return currentBeanScoped;
	}

}
