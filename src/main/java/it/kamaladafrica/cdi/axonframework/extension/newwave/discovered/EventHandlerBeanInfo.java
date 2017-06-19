package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class EventHandlerBeanInfo extends AbstractAnnotatedTypeInfo {

	public EventHandlerBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isEventHandler(annotatedType.getJavaClass()),
				"Bean is not an event handler: " + annotatedType.getJavaClass().getName());
	}

}
