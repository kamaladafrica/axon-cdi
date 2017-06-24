package it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation;

import javax.enterprise.inject.spi.BeanManager;

public interface BeanScopeValidator {

	void validate(BeanManager beanManager) throws BeanScopeNotValidException;

}
