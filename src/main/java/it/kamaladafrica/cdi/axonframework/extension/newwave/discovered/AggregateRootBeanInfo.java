package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class AggregateRootBeanInfo extends AbstractAnnotatedTypeInfo {

	public AggregateRootBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isAnnotatedAggregateRoot(annotatedType.getJavaClass()),
				"Bean is not an aggregate root: " + annotatedType.getJavaClass().getName());
	}

}
