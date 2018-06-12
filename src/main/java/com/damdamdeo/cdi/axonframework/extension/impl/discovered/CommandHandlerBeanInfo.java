package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.commons.lang3.Validate;

import com.damdamdeo.cdi.axonframework.support.AxonUtils;

public class CommandHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public CommandHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Validate.validState(AxonUtils.isCommandHandlerBean(annotatedType.getJavaClass()),
				"Bean is not an command handler: " + annotatedType.getJavaClass().getName());
	}

}
