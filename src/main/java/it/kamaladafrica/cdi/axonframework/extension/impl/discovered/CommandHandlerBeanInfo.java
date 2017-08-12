package it.kamaladafrica.cdi.axonframework.extension.impl.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class CommandHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public CommandHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isCommandHandlerBean(annotatedType.getJavaClass()),
				"Bean is not an command handler: " + annotatedType.getJavaClass().getName());
	}

}
