package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

import org.axonframework.config.Configuration;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractBeansCreation implements BeansCreation {

	private final BeansCreation original;

	public AbstractBeansCreation(final BeansCreation original) {
		this.original = Objects.requireNonNull(original);
	}

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
				final BeanManager beanManager, final Set<Annotation> qualifiers,
				final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifiers);
		qualifiers.forEach(qualifier -> {
			if (qualifier.getClass().isAnnotationPresent(Qualifier.class)) {
				throw new IllegalArgumentException(String.format("'%s' is not a qualifier", qualifier.getClass().getSimpleName()));
			}
		});
		Set<Annotation> normalizedQualifiers = CdiUtils.normalizedQualifiers(qualifiers);
		original.create(afterBeanDiscovery, beanManager, normalizedQualifiers, configuration);
		Set<Bean<?>> newBeansToAdd = concreateCreateBean(beanManager, normalizedQualifiers, configuration);
		newBeansToAdd.stream().forEach(newBeanToAdd -> afterBeanDiscovery.addBean(newBeanToAdd));
	}

	protected abstract Set<Bean<?>> concreateCreateBean(BeanManager beanManager, Set<Annotation> normalizedQualifiers, Configuration configuration);

}
