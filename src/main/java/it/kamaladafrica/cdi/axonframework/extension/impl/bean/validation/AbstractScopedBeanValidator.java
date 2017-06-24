package it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.literal.AnyLiteral;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractScopedBeanValidator implements BeanScopeValidator {

	private final BeanScopeValidator beanScopeValidator;

	private final Class<?> targetClazz;

	public AbstractScopedBeanValidator(final BeanScopeValidator beanScopeValidator,
			final Class<?> targetClazz) {
		this.beanScopeValidator = Objects.requireNonNull(beanScopeValidator);
		this.targetClazz = Objects.requireNonNull(targetClazz);
	}

	@Override
	public void validate(final BeanManager beanManager) throws BeanScopeNotValidException {
		Set<Bean<?>> beans = CdiUtils.getBeans(beanManager, targetClazz, new AnyLiteral());
		for (Bean<?> bean : beans) {
			if (!bean.getScope().equals(targetScoped())) {
				throw new BeanScopeNotValidException(bean,
						targetScoped(),
						bean.getScope());
			}
		}
		beanScopeValidator.validate(beanManager);
	}

	protected abstract Class<? extends Annotation> targetScoped();

}
