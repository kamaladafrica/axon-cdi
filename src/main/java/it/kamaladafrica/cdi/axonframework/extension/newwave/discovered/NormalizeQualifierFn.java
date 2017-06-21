package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.base.Function;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

class NormalizeQualifierFn implements Function<Set<Annotation>, Set<Annotation>> {

	static final Function<Set<Annotation>, Set<Annotation>> INSTANCE = new NormalizeQualifierFn();

	private NormalizeQualifierFn() {}

	@Override
	public Set<Annotation> apply(final Set<Annotation> input) {
		return CdiUtils.normalizedQualifiers(input);
	}

}
