package it.kamaladafrica.cdi.axonframework.extension.impl;

import static it.kamaladafrica.cdi.axonframework.support.CdiUtils.normalizedQualifiers;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.base.Function;

class NormalizeQualifierFn implements Function<Set<Annotation>, Set<Annotation>> {

	static final Function<Set<Annotation>, Set<Annotation>> INSTANCE = new NormalizeQualifierFn();

	private NormalizeQualifierFn() {}

	@Override
	public Set<Annotation> apply(Set<Annotation> input) {
		return normalizedQualifiers(input);
	}
}
