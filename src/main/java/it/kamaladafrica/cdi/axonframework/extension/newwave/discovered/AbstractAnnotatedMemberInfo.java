package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.inject.Qualifier;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public abstract class AbstractAnnotatedMemberInfo {

	private final AnnotatedMember<?> annotatedMember;

	public AbstractAnnotatedMemberInfo(final AnnotatedMember<?> annotatedMember) {
		this.annotatedMember = Objects.requireNonNull(annotatedMember);
	}

	public Set<Annotation> normalizedQualifiers() {
		Set<Annotation> qualifiers = annotatedMember
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

}
