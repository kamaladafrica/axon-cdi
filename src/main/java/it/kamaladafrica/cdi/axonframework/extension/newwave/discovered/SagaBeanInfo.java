package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class SagaBeanInfo extends AbstractAnnotatedTypeInfo {

	public SagaBeanInfo(final AnnotatedType<?> annotatedType) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isAnnotatedSaga(annotatedType.getJavaClass()),
				"Bean is not a saga: " + annotatedType.getJavaClass().getName());
	}

}
