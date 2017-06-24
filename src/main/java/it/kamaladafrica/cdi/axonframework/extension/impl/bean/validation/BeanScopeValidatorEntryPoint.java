package it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation;

import javax.enterprise.inject.spi.BeanManager;

public class BeanScopeValidatorEntryPoint implements BeanScopeValidator {

	@Override
	public void validate(final BeanManager beanManager) throws BeanScopeNotValidException {
		// nothing to do... just an entrypoint
	}

}
