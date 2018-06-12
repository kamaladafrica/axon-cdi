package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.commons.lang3.Validate;

import com.damdamdeo.cdi.axonframework.support.AxonUtils;

public class EventHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public EventHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Validate.validState(AxonUtils.isEventHandlerBean(annotatedType.getJavaClass()),
				"Bean is not an event handler: " + annotatedType.getJavaClass().getName());
	}

}
