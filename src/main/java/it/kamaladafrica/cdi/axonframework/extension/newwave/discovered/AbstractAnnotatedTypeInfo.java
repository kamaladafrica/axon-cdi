package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Qualifier;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractAnnotatedTypeInfo {

	private final AnnotatedType<?> annotatedType;

	public AbstractAnnotatedTypeInfo(final AnnotatedType<?> annotatedType) {
		this.annotatedType = Objects.requireNonNull(annotatedType);
	}

	public AnnotatedType<?> annotatedType() {
		return annotatedType;
	}

	public Set<Annotation> normalizedQualifiers() {
		Set<Annotation> qualifiers = annotatedType
				.getAnnotations()
				.stream()
				.filter(new Predicate<Annotation>() {

					@Override
					public boolean test(final Annotation annotation) {
						return annotation.annotationType().isAnnotationPresent(Qualifier.class);
					}

				}).collect(Collectors.toSet());
		return CdiUtils.normalizedQualifiers(qualifiers);
	}

	public Class<?> type() {
		return this.annotatedType.getJavaClass();
	}

}
