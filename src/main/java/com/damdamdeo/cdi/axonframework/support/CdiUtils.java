package com.damdamdeo.cdi.axonframework.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.BeanUtils;
import org.apache.deltaspike.core.util.HierarchyDiscovery;

import com.damdamdeo.cdi.axonframework.DefaultQualifierMeme;
import com.damdamdeo.cdi.axonframework.InheritQualifiers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class CdiUtils {

	public static final Any ANY_LITERAL = new AnyLiteral();

	public static final Default DEFAULT_LITERAL = new DefaultLiteral();

	private static final Class<?> DEFAULT_MARKER = DefaultQualifierMeme.class;

	private static final Class<?> INHERIT_MARKER = InheritQualifiers.class;

	private static final Set<Annotation> DEFAULT_QUALIFIERS = ImmutableSet
			.<Annotation> of(DEFAULT_LITERAL, ANY_LITERAL);

	private CdiUtils() {}

	public static Set<Annotation> qualifiers(final BeanManager bm, final Iterable<Annotation> annotations) {
		return ImmutableSet.copyOf(BeanUtils.getQualifiers(bm, annotations));
	}

	public static Set<Annotation> qualifiers(final BeanManager bm, final Annotation[] annotations) {
		return qualifiers(bm, Arrays.asList(annotations));
	}

	public static Set<Annotation> qualifiers(final BeanManager bm, final Annotated annotated) {
		return qualifiers(bm, annotated.getAnnotations());
	}

	public static Set<Annotation> qualifiers(final BeanManager bm, final Class<?> javaClass) {
		return qualifiers(bm, javaClass.getAnnotations());
	}

	public static boolean qualifiersMatch(final Set<Annotation> qualifiers,
			final Set<Annotation> otherQualifiers) {
		return normalizedQualifiers(qualifiers).equals(normalizedQualifiers(otherQualifiers));
	}

	public static Set<Annotation> normalizedQualifiers(final Set<Annotation> qualifiers) {
		switch (qualifiers.size()) {
		case 0:
			return DEFAULT_QUALIFIERS;
		case 1:
			if (qualifiers.contains(ANY_LITERAL)) {
				return DEFAULT_QUALIFIERS;
			}
			break;
		case 2:
			if (DEFAULT_QUALIFIERS.equals(qualifiers)) {
				return DEFAULT_QUALIFIERS;
			}
			break;
		}
		return ImmutableSet.<Annotation> builder().addAll(qualifiers).add(ANY_LITERAL).build();
	}

	public static boolean isDefaultMarker(final Class<?> marker) {
		return DEFAULT_MARKER.equals(marker);
	}

	public static boolean isInheritMarker(final Class<?> marker) {
		return INHERIT_MARKER.equals(marker);
	}

	public static Object getReference(final BeanManager bm, final Type type, final Set<Annotation> qualifiers) {
		Annotation[] annotations = Iterables.toArray(qualifiers, Annotation.class);
		return getReference(bm, type, annotations);
	}

	public static Object getReference(final BeanManager bm, final Type type, final Annotation... qualifiers) {
		Set<Bean<?>> beans = getBeans(bm, type, qualifiers);
		Bean<?> bean = bm.resolve(beans);
		return bm.getReference(bean, type, bm.createCreationalContext(null));
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReference(final BeanManager bm, final Bean<T> bean, final Type beanType) {
		return (T) bm.getReference(bean, beanType, bm.createCreationalContext(null));
	}

	public static Set<Bean<?>> getBeans(final BeanManager bm, final Type type, final Set<Annotation> qualifiers) {
		Annotation[] annotations = Iterables.toArray(qualifiers, Annotation.class);
		return bm.getBeans(type, annotations);
	}

	public static Set<Bean<?>> getBeans(final BeanManager bm, final Type type, final Annotation... qualifiers) {
		return bm.getBeans(type, qualifiers);
	}

	public static Bean<?> getBean(final BeanManager bm, final Type type, final Set<Annotation> qualifiers) {
		Annotation[] annotations = Iterables.toArray(qualifiers, Annotation.class);
		return getBean(bm, type, annotations);
	}

	public static Bean<?> getBean(final BeanManager bm, final Type type, final Annotation... qualifiers) {
		Set<Bean<?>> beans = getBeans(bm, type, qualifiers);
		return bm.resolve(beans);
	}

	@SuppressWarnings("unchecked")
	public static <T> T injectFields(final BeanManager beanManager, final T instance) {
		if (instance == null) {
			return null;
		}

		CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);

		AnnotatedType<T> annotatedType = beanManager
				.createAnnotatedType((Class<T>) instance.getClass());
		InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);
		injectionTarget.inject(instance, creationalContext);
		return instance;
	}

	public static Set<Type> typeClosure(final Type type) {
		return new HierarchyDiscovery(type).getTypeClosure();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T findAnnotation(final BeanManager beanManager,
			final Annotation[] annotations, final Class<T> targetAnnotationType) {
		for (Annotation annotation : annotations) {
			if (targetAnnotationType.equals(annotation.annotationType())) {
				return (T) annotation;
			}
			if (beanManager.isStereotype(annotation.annotationType())) {
				T result = findAnnotation(beanManager, annotation.annotationType().getAnnotations(),
						targetAnnotationType);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public static <T extends Annotation> T findAnnotation(final BeanManager beanManager,
			final Iterable<Annotation> annotations, final Class<T> targetAnnotationType) {
		return findAnnotation(beanManager, Iterables.toArray(annotations, Annotation.class),
				targetAnnotationType);
	}

}
