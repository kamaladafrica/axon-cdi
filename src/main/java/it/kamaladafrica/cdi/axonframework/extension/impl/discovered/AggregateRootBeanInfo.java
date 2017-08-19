package it.kamaladafrica.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import it.kamaladafrica.cdi.axonframework.AggregateConfiguration;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.commandbus.CommandBusProxified;

public class AggregateRootBeanInfo {

	public static enum QualifierType {

		DEFAULT(Object.class),
		COMMAND_BUS(CommandBusProxified.class),
		COMMAND_GATEWAY(CommandGateway.class),
		EVENT_BUS(EventBus.class),
		SNAPSHOTTER_TRIGGER_DEFINITION(SnapshotTriggerDefinition.class),
		TOKEN_STORE(TokenStore.class),
		TRANSACTION_MANAGER(TransactionManager.class),
		SERIALIZER(Serializer.class),
		EVENT_STORAGE_ENGINE(EventStorageEngine.class),
		EVENT_SCHEDULER(EventScheduler.class),
		CORRELATION_DATA_PROVIDER(CorrelationDataProvider.class);

		Class<?> clazz;

		private QualifierType(final Class<?> clazz) {
			this.clazz = clazz;
		}

	}

	private final AnnotatedType<?> annotatedType;

	private final Class<?> type;

	private final Map<QualifierType, Set<Annotation>> qualifiers;

	private AggregateRootBeanInfo(final AnnotatedType<?> annotatedType, final Map<QualifierType, Set<Annotation>> qualifiers) {
		this.annotatedType = Objects.requireNonNull(annotatedType);
		this.type = annotatedType.getJavaClass();
		Preconditions.checkArgument(AxonUtils.isAnnotatedAggregateRoot(type), "Bean is not an aggregate root: " + type);
		this.qualifiers = Collections.unmodifiableMap(qualifiers);
	}

	public static AggregateRootBeanInfo of(final BeanManager beanManager, final AnnotatedType<?> annotatedType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(annotatedType);
		Map<QualifierType, Set<Annotation>> qualifiers = extractQualifiers(beanManager, annotatedType);
		return new AggregateRootBeanInfo(annotatedType, qualifiers);
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

	public boolean isSameContext(final AggregateRootBeanInfo aggregateRootBeanInfo) {
		Objects.requireNonNull(aggregateRootBeanInfo);
		return qualifiers.get(QualifierType.COMMAND_BUS).equals(aggregateRootBeanInfo.qualifiers(QualifierType.COMMAND_BUS))
				&& qualifiers.get(QualifierType.COMMAND_GATEWAY).equals(aggregateRootBeanInfo.qualifiers(QualifierType.COMMAND_GATEWAY))
				&& qualifiers.get(QualifierType.EVENT_BUS).equals(aggregateRootBeanInfo.qualifiers(QualifierType.EVENT_BUS));
	}

	public boolean isSameContext(final SagaBeanInfo sagaBeanInfo) {
		Objects.requireNonNull(sagaBeanInfo);
		return qualifiers.get(QualifierType.EVENT_BUS).equals(sagaBeanInfo.qualifiers(SagaBeanInfo.QualifierType.EVENT_BUS));
	}

	public boolean isSameContext(final CommandHandlerBeanInfo commandHandlerBeanInfo) {
		Objects.requireNonNull(commandHandlerBeanInfo);
		return qualifiers.get(QualifierType.COMMAND_BUS).equals(commandHandlerBeanInfo.normalizedQualifiers());
	}

	public boolean isSameContext(final EventHandlerBeanInfo eventHandlerBeanInfo) {
		Objects.requireNonNull(eventHandlerBeanInfo);
		return qualifiers.get(QualifierType.EVENT_BUS).equals(eventHandlerBeanInfo.normalizedQualifiers());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static Map<QualifierType, Set<Annotation>> extractQualifiers(final BeanManager beanManager,
			final AnnotatedType<?> annotatedType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(annotatedType);
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		AggregateConfiguration aggregateConfiguration = CdiUtils.findAnnotation(beanManager,
				annotatedType.getAnnotations(), AggregateConfiguration.class);
		if (aggregateConfiguration != null) {
			qualifiers.putAll(
					extractQualifiers(beanManager, aggregateConfiguration, annotatedType.getJavaClass()));
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
			final BeanManager beanManager, final AggregateConfiguration aggregateConfiguration, final Class<?> aggregateType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateConfiguration);
		Objects.requireNonNull(aggregateType);		
		Map<QualifierType, Set<Annotation>> qualifiers = new HashMap<>();
		Class<?> fallback = CdiUtils.isInheritMarker(aggregateConfiguration.value()) ? aggregateType : aggregateConfiguration.value();
		addQualifiers(beanManager, qualifiers, QualifierType.DEFAULT, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.COMMAND_BUS, aggregateConfiguration.commandBus(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.COMMAND_GATEWAY, aggregateConfiguration.commandBus(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.EVENT_BUS, aggregateConfiguration.eventBus(), fallback);

		// These qualifiers use only default value because only one instance (or multiple instances having the same interface and the same qualifier)
		// can be defined per configurer
		addQualifiers(beanManager, qualifiers, QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.TOKEN_STORE, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.TRANSACTION_MANAGER, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.SERIALIZER, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.EVENT_STORAGE_ENGINE, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.EVENT_SCHEDULER, aggregateConfiguration.value(), fallback);
		addQualifiers(beanManager, qualifiers, QualifierType.CORRELATION_DATA_PROVIDER, aggregateConfiguration.value(), fallback);
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

	public boolean hasBean(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				qualifierType.clazz,
				qualifiers(qualifierType))).isPresent();
	}

	public Bean<?> resolveBean(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifierType);
		return CdiUtils.getBean(beanManager, qualifierType.clazz, qualifiers(qualifierType));
	}

	public Object getReference(final BeanManager beanManager, final QualifierType qualifierType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(qualifierType);
		return CdiUtils.getReference(beanManager, qualifierType.clazz, qualifiers(qualifierType));
	}

	public Class<?> type() {
		return type;
	}

	public AnnotatedType<?> annotatedType() {
		return annotatedType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotatedType == null) ? 0 : annotatedType.hashCode());
		result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		AggregateRootBeanInfo other = (AggregateRootBeanInfo) obj;
		if (annotatedType == null) {
			if (other.annotatedType != null)
				return false;
		} else if (!annotatedType.equals(other.annotatedType))
			return false;
		if (qualifiers == null) {
			if (other.qualifiers != null)
				return false;
		} else if (!qualifiers.equals(other.qualifiers))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
