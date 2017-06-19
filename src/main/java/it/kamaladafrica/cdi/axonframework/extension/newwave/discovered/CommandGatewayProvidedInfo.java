package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import javax.enterprise.inject.spi.AnnotatedMember;

import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

public class CommandGatewayProvidedInfo extends AbstractAnnotatedMemberInfo {

	public CommandGatewayProvidedInfo(final AnnotatedMember<?> annotatedMember) {
		super(annotatedMember);
		Preconditions.checkArgument(AxonUtils.isCommandGateway((Class<?>) annotatedMember.getBaseType()),
				"Provided type is not a command gateway: " + annotatedMember.getBaseType().getTypeName());
	}

}
