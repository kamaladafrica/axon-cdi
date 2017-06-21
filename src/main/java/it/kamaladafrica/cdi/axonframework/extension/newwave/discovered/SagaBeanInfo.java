package it.kamaladafrica.cdi.axonframework.extension.newwave.discovered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.saga.repository.SagaStore;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import it.kamaladafrica.cdi.axonframework.SagaConfiguration;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class SagaBeanInfo extends AbstractAnnotatedTypeInfo {

	public interface ParameterizedQualifierType {

		Type parameterizedType();

	}

	public static enum QualifierType implements ParameterizedQualifierType {

		DEFAULT(Object.class) {

			@Override
			public Type parameterizedType() {
				throw new UnsupportedOperationException("Object class is not parameterized !");
			}
			
		},
		EVENT_BUS(EventBus.class) {

			public Type parameterizedType() {
				throw new UnsupportedOperationException("EventBus class is not parameterized !");
			}
			
		},
		SAGA_STORE(SagaStore.class) {

			public Type parameterizedType() {
				return TypeUtils.parameterize(SagaStore.class,
						clazz);
			}
			
		};

		Class<?> clazz;

		private QualifierType(final Class<?> clazz) {
			this.clazz = clazz;
		}

	}

	private final AnnotatedType<?> annotatedType;

	private final Class<?> type;

	private final Map<QualifierType, Set<Annotation>> qualifiers;

	public SagaBeanInfo(final AnnotatedType<?> annotatedType, final Map<QualifierType, Set<Annotation>> qualifiers) {
		super(annotatedType);
		Preconditions.checkArgument(AxonUtils.isAnnotatedSaga(annotatedType.getJavaClass()),
				"Bean is not a saga: " + annotatedType.getJavaClass().getName());
		this.annotatedType = Objects.requireNonNull(annotatedType);
		this.type = annotatedType.getJavaClass();
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	public static SagaBeanInfo of(final BeanManager beanManager, final AnnotatedType<?> annotatedType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(annotatedType);
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(beanManager, annotatedType);
		return new SagaBeanInfo(annotatedType, qualifiers);
	}

	public Set<Annotation> qualifiers(final QualifierType qualifierType) {
		Objects.requireNonNull(qualifierType);
		return qualifiers.get(qualifierType);
	}

	public Map<QualifierType, Set<Annotation>> qualifiers() {
		return qualifiers;
	}

	public boolean matchQualifiers(final QualifierType qualifierType, final Set<Annotation> qualifiers) {
		Objects.requireNonNull(qualifierType);
		Objects.requireNonNull(qualifiers);
		return qualifiers(qualifierType).equals(CdiUtils.normalizedQualifiers(qualifiers));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static Map<QualifierType, Set<Annotation>> extractQualifiers(final BeanManager beanManager,
			final AnnotatedType<?> annotatedType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(annotatedType);
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		SagaConfiguration sagaConfiguration = CdiUtils.findAnnotation(beanManager,
				annotatedType.getAnnotations(), SagaConfiguration.class);
		if (sagaConfiguration != null) {
			qualifiers.putAll(
					extractQualifiers(beanManager, sagaConfiguration, annotatedType.getJavaClass()));
		} else {
			Set<Annotation> defaultQualifiers = CdiUtils.qualifiers(beanManager, annotatedType);
			for (QualifierType type : QualifierType.values()) {
				qualifiers.put(type, defaultQualifiers);
			}
		}
		return normalizeQualifiers(qualifiers);
	}

	private static Map<QualifierType, Set<Annotation>> normalizeQualifiers(
			final Map<QualifierType, Set<Annotation>> map) {
		Objects.requireNonNull(map);
		return Maps.newHashMap(Maps.transformValues(map, NormalizeQualifierFn.INSTANCE));
	}

	private static Map<? extends QualifierType, ? extends Set<Annotation>> extractQualifiers(
			final BeanManager beanManager, final SagaConfiguration sagaConfiguration, final Class<?> aggregateType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(sagaConfiguration);
		Objects.requireNonNull(aggregateType);		
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		Class<?> fallback = CdiUtils.isInheritMarker(sagaConfiguration.value()) ? aggregateType : sagaConfiguration.value();
		addQualifiers(beanManager, qualifiers, QualifierType.DEFAULT, sagaConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.EVENT_BUS, sagaConfiguration.eventBus(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.SAGA_STORE, sagaConfiguration.sagaStore(), fallback);
		return qualifiers;
	}

	private static void addQualifiers(final BeanManager beanManager,
			final Map<QualifierType, Set<Annotation>> qualifiers, final QualifierType type, final Class<?> qualifier,
			final Class<?> fallback) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifiers);
		Objects.requireNonNull(type);
		Objects.requireNonNull(qualifier);
		Objects.requireNonNull(fallback);
		qualifiers.put(type, CdiUtils.qualifiers(beanManager, CdiUtils.isInheritMarker(qualifier) ? fallback : qualifier));
	}

	public Set<Bean<?>> getBeans(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifierType);
		Set<Annotation> qualifierSet = qualifiers(qualifierType);
		final Annotation[] qualifiers = Iterables.toArray(qualifierSet, Annotation.class);
		return beanManager.getBeans(qualifierType.clazz, qualifiers);
	}

	public Set<Bean<?>> getSagaStoreBeans(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		Set<Annotation> qualifierSet = qualifiers(QualifierType.SAGA_STORE);
		final Annotation[] qualifiers = Iterables.toArray(qualifierSet, Annotation.class);
		return beanManager.getBeans(QualifierType.SAGA_STORE.parameterizedType(), qualifiers);
	}

	public Bean<?> resolveBean(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifierType);
		Set<Bean<?>> beans = getBeans(beanManager, qualifierType);
		return beanManager.resolve(beans);
	}

	public Bean<?> resolveSagaStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		Set<Bean<?>> beans = getSagaStoreBeans(beanManager);
		return beanManager.resolve(beans);
	}

	public Object getReference(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifierType);
		Bean<?> bean = resolveBean(beanManager, qualifierType);
		return beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(null));
	}

	public Object getReference(final BeanManager beanManager, final Bean<?> bean) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(bean);
		return beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(null));
	}

	public Class<?> type() {
		return type;
	}

	public AnnotatedType<?> annotatedType() {
		return annotatedType;
	}

}
