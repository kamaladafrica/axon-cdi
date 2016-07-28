package org.axonframework.integration.cdi;

import javax.enterprise.inject.Default;

@TestQualifier
public interface GlobalQualifierDefinitions {

	@TestQualifier
	public interface CommandBus {}

	@Default
	public interface EventBus {}

}
