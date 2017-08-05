package it.kamaladafrica.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo.QualifierType;

public class ExecutionContext {

	private List<AggregateRootBeanInfo> aggregateRootBeanInfos = new ArrayList<>();

	private List<SagaBeanInfo> sagaBeanInfos = new ArrayList<>();

	private List<CommandHandlerBeanInfo> commandHandlerBeanInfos = new ArrayList<>();

	private List<EventHandlerBeanInfo> eventHandlerBeanInfos = new ArrayList<>();

	private final MetricRegistry metricRegistry;

	public ExecutionContext(final AggregateRootBeanInfo aggregateRootBeanInfo) {
		Objects.requireNonNull(aggregateRootBeanInfo);
		this.aggregateRootBeanInfos.add(aggregateRootBeanInfo);
		this.metricRegistry = new MetricRegistry();
	}

	public boolean registerIfSameContext(final AggregateRootBeanInfo aggregateRootBeanInfo) {
		Objects.requireNonNull(aggregateRootBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(aggregateRootBeanInfo)) {
			aggregateRootBeanInfos.add(aggregateRootBeanInfo);
			return true;
		}
		return false;
	}

	public boolean registerIfSameContext(final SagaBeanInfo sagaBeanInfo) {
		Objects.requireNonNull(sagaBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(sagaBeanInfo)) {
			sagaBeanInfos.add(sagaBeanInfo);
			return true;
		}
		return false;
	}

	public boolean registerIfSameContext(final CommandHandlerBeanInfo commandHandlerBeanInfo) {
		Objects.requireNonNull(commandHandlerBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(commandHandlerBeanInfo)) {
			commandHandlerBeanInfos.add(commandHandlerBeanInfo);
			return true;
		}
		return false;
	}

	public boolean registerIfSameContext(final EventHandlerBeanInfo eventHandlerBeanInfo) {
		Objects.requireNonNull(eventHandlerBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(eventHandlerBeanInfo)) {
			eventHandlerBeanInfos.add(eventHandlerBeanInfo);
			return true;
		}
		return false;
	}

	public List<AggregateRootBeanInfo> aggregateRootBeanInfos() {
		return Collections.unmodifiableList(aggregateRootBeanInfos);
	}

	public List<SagaBeanInfo> sagaBeanInfos() {
		return Collections.unmodifiableList(sagaBeanInfos);
	}

	public List<CommandHandlerBeanInfo> commandHandlerBeanInfos() {
		return Collections.unmodifiableList(commandHandlerBeanInfos);
	}

	public List<EventHandlerBeanInfo> eventHandlerBeanInfos() {
		return Collections.unmodifiableList(eventHandlerBeanInfos);
	}

	public Set<Annotation> commandGatewayQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.COMMAND_GATEWAY);
	}

	public Set<Annotation> commandBusQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.COMMAND_BUS);
	}

	public Set<Annotation> eventSchedulerQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.EVENT_SCHEDULER);
	}

	public Set<Annotation> snapshotterTriggerDefinitionQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION);
	}

	public boolean hasAnEventSchedulerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_SCHEDULER);
	}

	public boolean hasACommandGatewayBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.COMMAND_GATEWAY);
	}

	public boolean hasACommandBusBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.COMMAND_BUS);
	}

	public boolean hasAnEventStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_BUS);
	}

	public boolean hasAnEventStorageEngineBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
	}

	public boolean hasATransactionManagerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.TRANSACTION_MANAGER);
	}

	public boolean hasATokenStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.TOKEN_STORE);
	}

	public boolean hasASerializerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.SERIALIZER);
	}

	public boolean hasACorrelationDataProviderBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.CORRELATION_DATA_PROVIDER);
	}

	public CommandBus getCommandBusReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (CommandBus) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.COMMAND_BUS);
	}

	// EventStore extends EventBus
	public EventStore getEventStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStore) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.EVENT_BUS);
	}

	public EventStorageEngine getEventStorageEngineReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStorageEngine) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
	}

	public TransactionManager getTransactionManagerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TransactionManager) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.TRANSACTION_MANAGER);
	}

	public TokenStore getTokenStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TokenStore) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.TOKEN_STORE);
	}

	public SnapshotTriggerDefinition getSnapshotTriggerDefinitionReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (SnapshotTriggerDefinition) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION);
	}

	public Serializer getSerializerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (Serializer) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.SERIALIZER);
	}

	public CorrelationDataProvider getCorrelationDataProviderReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (CorrelationDataProvider) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.CORRELATION_DATA_PROVIDER);
	}

	public MetricRegistry metricRegistry() {
		return metricRegistry;
	}

}
