package it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;

public class DependentScopedBeanValidator extends AbstractScopedBeanValidator {

	public DependentScopedBeanValidator(final BeanScopeValidator beanScopeValidator, final Class<?> targetClazz) {
		super(beanScopeValidator, targetClazz);
	}

	@Override
	protected Class<? extends Annotation> targetScoped() {
		return Dependent.class;
	}

}
