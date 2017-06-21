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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotatedMember == null) ? 0 : annotatedMember.hashCode());
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
		AbstractAnnotatedMemberInfo other = (AbstractAnnotatedMemberInfo) obj;
		if (annotatedMember == null) {
			if (other.annotatedMember != null)
				return false;
		} else if (!annotatedMember.equals(other.annotatedMember))
			return false;
		return true;
	}

}
