package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.damdamdeo.cdi.axonframework.support.AxonUtils;
import com.google.common.base.Preconditions;

public class EventHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public EventHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isEventHandlerBean(annotatedType.getJavaClass()),
				"Bean is not an event handler: " + annotatedType.getJavaClass().getName());
	}

}
