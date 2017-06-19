package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import javax.enterprise.inject.spi.AnnotatedMember;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class CorrelationDataProvidedInfo extends AbstractAnnotatedMemberInfo {

	public CorrelationDataProvidedInfo(final AnnotatedMember<?> annotatedMember) {
		super(annotatedMember);
		Preconditions.checkArgument(AxonUtils.isCorrelationDataProvider((Class<?>) annotatedMember.getBaseType()),
				"Provided type is not a correlation data provider: " + annotatedMember.getBaseType().getTypeName());
	}


}
