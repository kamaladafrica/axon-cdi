package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractAnnotatedTypeInfo {

	private final AnnotatedType<?> annotatedType;
	private final Set<Annotation> normalizedQualifiers;

	public AbstractAnnotatedTypeInfo(final AnnotatedType<?> annotatedType) {
		this.annotatedType = Objects.requireNonNull(annotatedType);
		this.normalizedQualifiers = CdiUtils.normalizedQualifiers(annotatedType
				.getAnnotations()
				.stream()
				.filter(new Predicate<Annotation>() {

					@Override
					public boolean test(final Annotation annotation) {
						return annotation.annotationType().isAnnotationPresent(Qualifier.class);
					}

				}).collect(Collectors.toSet()));
	}

	public AnnotatedType<?> annotatedType() {
		return annotatedType;
	}

	public Set<Annotation> normalizedQualifiers() {
		return normalizedQualifiers;
	}

	public Class<?> type() {
		return this.annotatedType.getJavaClass();
	}

	public Object getReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return CdiUtils.getReference(beanManager, type(), normalizedQualifiers());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotatedType == null) ? 0 : annotatedType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAnnotatedTypeInfo other = (AbstractAnnotatedTypeInfo) obj;
		if (annotatedType == null) {
			if (other.annotatedType != null)
				return false;
		} else if (!annotatedType.equals(other.annotatedType))
			return false;
		return true;
	}

}
