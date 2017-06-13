package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.SagaConfiguration;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SagaInfo extends AxonConfigInfo {

	public static enum QualifierType {
		DEFAULT, REPOSITORY, EVENT_BUS, FACTORY
	}

	private final Map<QualifierType, Set<Annotation>> qualifiers;

	public SagaInfo(final Class<?> type, final Map<QualifierType, Set<Annotation>> qualifiers) {
		super(type);
		Preconditions.checkArgument(AxonUtils.isAnnotatedSaga(type),
				"Provided type is not a saga: " + type.getName());
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	public SagaInfo(final Bean<?> bean, final Map<QualifierType, Set<Annotation>> qualifiers) {
		super(bean);
		Preconditions.checkArgument(AxonUtils.isAnnotatedSaga(bean.getBeanClass()),
				"Provided type is not a saga: " + bean.getBeanClass().getName());
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	public Set<Annotation> getQualifiers(final QualifierType type) {
		return qualifiers.get(type);
	}

	public Map<QualifierType, Set<Annotation>> getQualifiers() {
		return qualifiers;
	}

	public Set<Bean<?>> getBeans(final BeanManager bm, final QualifierType type) {
		return super.getBeans(bm, getQualifiers(type));
	}

	public Bean<?> resolveBean(final BeanManager bm, final QualifierType type) {
		return super.resolveBean(bm, getQualifiers(type));
	}

	public Object getReference(final BeanManager bm, final QualifierType type) {
		return super.getReference(bm, getQualifiers(type));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SagaInfo other = (SagaInfo) obj;
		if (qualifiers == null) {
			if (other.qualifiers != null) {
				return false;
			}
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		}
		return true;
	}

	public static SagaInfo of(final BeanManager bm, AnnotatedType<?> annotated) {
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(bm, annotated);
		return new SagaInfo(annotated.getJavaClass(), qualifiers);
	}

	public static SagaInfo of(final BeanManager bm, final Bean<?> bean) {
		AnnotatedType<?> annotated = bm.createAnnotatedType(bean.getBeanClass());
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(bm, annotated);
		return new SagaInfo(bean, qualifiers);
	}

	private static Map<QualifierType, Set<Annotation>> extractQualifiers(final BeanManager bm,
			final AnnotatedType<?> annotated) {
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		SagaConfiguration sagaConfiguration = CdiUtils.findAnnotation(bm, annotated.getAnnotations(),
				SagaConfiguration.class);
		if (sagaConfiguration != null) {
			qualifiers.putAll(extractQualifiers(bm, sagaConfiguration, annotated.getJavaClass()));
		} else {
			Set<Annotation> defaultQualifiers = CdiUtils.qualifiers(bm, annotated);
			for (QualifierType type : QualifierType.values()) {
				qualifiers.put(type, defaultQualifiers);
			}
		}
		return normalizeQualifiers(qualifiers);
	}

	private static Map<QualifierType, Set<Annotation>> normalizeQualifiers(
			final Map<QualifierType, Set<Annotation>> map) {
		return Maps.newHashMap(Maps.transformValues(map, NormalizeQualifierFn.INSTANCE));
	}

	private static Map<? extends QualifierType, ? extends Set<Annotation>> extractQualifiers(
			final BeanManager bm, final SagaConfiguration ac, final Class<?> aggregateType) {
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		Class<?> fallback = CdiUtils.isInheritMarker(ac.value()) ? aggregateType : ac.value();
		qualifiers.put(QualifierType.DEFAULT, CdiUtils.qualifiers(bm, fallback));
		addQualifiers(bm, qualifiers, QualifierType.EVENT_BUS, ac.eventBus(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.REPOSITORY, ac.repository(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.FACTORY, ac.factory(), fallback);
		return qualifiers;
	}

	private static void addQualifiers(final BeanManager bm,
			final Map<QualifierType, Set<Annotation>> qualifiers, final QualifierType type, final Class<?> qualifier,
			final Class<?> fallback) {
		qualifiers.put(type, CdiUtils.qualifiers(bm, CdiUtils.isInheritMarker(qualifier) ? fallback : qualifier));
	}

}
