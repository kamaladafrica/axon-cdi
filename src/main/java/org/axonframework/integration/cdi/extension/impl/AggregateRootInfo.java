package org.axonframework.integration.cdi.extension.impl;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static org.axonframework.integration.cdi.support.CdiUtils.findAnnotation;
import static org.axonframework.integration.cdi.support.CdiUtils.isInheritMarker;
import static org.axonframework.integration.cdi.support.CdiUtils.normalizedQualifiers;
import static org.axonframework.integration.cdi.support.CdiUtils.qualifiers;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.integration.cdi.AggregateConfiguration;
import org.axonframework.integration.cdi.support.AxonUtils;

import com.google.common.base.Preconditions;

public class AggregateRootInfo extends AxonConfigInfo {

	public static enum QualifierType {
		DEFAULT, REPOSITORY, EVENT_BUS, COMMAND_BUS, CONFLICT_RESOLVER, SNAPSHOTTER, SNAPSHOTTER_TRIGGER
	}

	private final Map<QualifierType, Set<Annotation>> qualifiers;

	private AggregateRootInfo(Bean<?> bean, Map<QualifierType, Set<Annotation>> qualifiers) {
		super(bean);
		Preconditions.checkArgument(AxonUtils.isAnnotatedAggregateRoot(bean.getBeanClass()));
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	private AggregateRootInfo(Class<?> type, Map<QualifierType, Set<Annotation>> qualifiers) {
		super(type);
		Preconditions.checkArgument(AxonUtils.isAnnotatedAggregateRoot(type));
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	public Set<Annotation> getQualifiers(QualifierType type) {
		return qualifiers.get(type);
	}

	public Map<QualifierType, Set<Annotation>> getQualifiers() {
		return qualifiers;
	}

	public boolean matchQualifiers(QualifierType type, Set<Annotation> qualifiers) {
		return getQualifiers(type).equals(normalizedQualifiers(qualifiers));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AggregateRootInfo other = (AggregateRootInfo) obj;
		if (qualifiers == null) {
			if (other.qualifiers != null) {
				return false;
			}
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		}
		return true;
	}

	public static AggregateRootInfo of(BeanManager bm, AnnotatedType<?> annotated) {
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(bm, annotated);
		return new AggregateRootInfo(annotated.getJavaClass(), qualifiers);
	}

	public static AggregateRootInfo of(BeanManager bm, Bean<?> bean) {
		AnnotatedType<?> annotated = bm.createAnnotatedType(bean.getBeanClass());
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(bm, annotated);
		return new AggregateRootInfo(bean, qualifiers);
	}

	private static Map<QualifierType, Set<Annotation>> extractQualifiers(BeanManager bm,
			AnnotatedType<?> annotated) {
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		AggregateConfiguration aggregateConfiguration = findAnnotation(bm,
				annotated.getAnnotations(), AggregateConfiguration.class);
		if (aggregateConfiguration != null) {
			qualifiers.putAll(
					extractQualifiers(bm, aggregateConfiguration, annotated.getJavaClass()));
		} else {
			Set<Annotation> defaultQualifiers = qualifiers(bm, annotated);
			for (QualifierType type : QualifierType.values()) {
				qualifiers.put(type, defaultQualifiers);
			}
		}
		return normalizeQualifiers(qualifiers);
	}

	private static Map<QualifierType, Set<Annotation>> normalizeQualifiers(
			Map<QualifierType, Set<Annotation>> map) {
		return newHashMap(transformValues(map, NormalizeQualifierFn.INSTANCE));
	}

	private static Map<? extends QualifierType, ? extends Set<Annotation>> extractQualifiers(
			BeanManager bm, AggregateConfiguration ac, Class<?> aggregateType) {
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		Class<?> fallback = isInheritMarker(ac.value()) ? aggregateType : ac.value();
		qualifiers.put(QualifierType.DEFAULT, qualifiers(bm, fallback));
		addQualifiers(bm, qualifiers, QualifierType.COMMAND_BUS, ac.commandBus(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.CONFLICT_RESOLVER, ac.conflictResolver(),
				fallback);
		addQualifiers(bm, qualifiers, QualifierType.EVENT_BUS, ac.eventBus(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.REPOSITORY, ac.repository(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.SNAPSHOTTER, ac.snapshotter(), fallback);
		addQualifiers(bm, qualifiers, QualifierType.SNAPSHOTTER_TRIGGER, ac.snapshotterTrigger(),
				fallback);
		return qualifiers;
	}

	private static void addQualifiers(BeanManager bm,
			Map<QualifierType, Set<Annotation>> qualifiers, QualifierType type, Class<?> qualifier,
			Class<?> fallback) {
		qualifiers.put(type, qualifiers(bm, isInheritMarker(qualifier) ? fallback : qualifier));
	}

	public Set<Bean<?>> getBeans(BeanManager bm, QualifierType type) {
		return super.getBeans(bm, getQualifiers(type));
	}

	public Bean<?> resolveBean(BeanManager bm, QualifierType type) {
		return super.resolveBean(bm, getQualifiers(type));
	}

	public Object getReference(BeanManager bm, QualifierType type) {
		return super.getReference(bm, getQualifiers(type));
	}

}
