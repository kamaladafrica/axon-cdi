package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.damdamdeo.cdi.axonframework.support.CdiUtils;
import com.google.common.base.Function;

class NormalizeQualifierFn implements Function<Set<Annotation>, Set<Annotation>> {

	static final Function<Set<Annotation>, Set<Annotation>> INSTANCE = new NormalizeQualifierFn();

	private NormalizeQualifierFn() {}

	@Override
	public Set<Annotation> apply(final Set<Annotation> input) {
		return CdiUtils.normalizedQualifiers(input);
	}

}
