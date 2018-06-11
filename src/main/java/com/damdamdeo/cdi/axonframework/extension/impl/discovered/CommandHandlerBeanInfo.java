package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.damdamdeo.cdi.axonframework.support.AxonUtils;
import com.google.common.base.Preconditions;

public class CommandHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public CommandHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isCommandHandlerBean(annotatedType.getJavaClass()),
				"Bean is not an command handler: " + annotatedType.getJavaClass().getName());
	}

}
