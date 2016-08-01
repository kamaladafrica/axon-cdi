package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.axonframework.eventsourcing.AbstractSnapshotter;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AutoConfiguringAggregateSnapshotter<X extends AbstractSnapshotter> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<AggregateRootInfo> aggregateRoots;

	public AutoConfiguringAggregateSnapshotter(Producer<X> wrappedProducer,
			AnnotatedMember<?> annotatedMember, Set<AggregateRootInfo> aggregateRoots,
			BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.aggregateRoots = aggregateRoots;
	}

	@Override
	protected X configure(X snapshotter) {
		//		SnapshotEventStore eventStore = eventStore();
		//		if(eventStore != null){
		//			snapshotter.setEventStore(eventStore);			
		//		}
		//
		//		Instance<Executor> executors = CDI.current().select(Executor.class);
		//		if (executors.isAmbiguous()) {
		//			executors = executors.select(getQualifiers());
		//		}
		//		if (!executors.isUnsatisfied()) {
		//			snapshotter.setExecutor(executors.get());
		//		}
		//
		//		@SuppressWarnings("serial")
		//		Instance<TransactionManager<?>> transactionManagers = CDI.current().select(
		//				new TypeLiteral<TransactionManager<?>>() {});
		//		if (transactionManagers.isAmbiguous()) {
		//			transactionManagers = transactionManagers.select(getQualifiers());
		//		}
		//		if (!transactionManagers.isUnsatisfied()) {
		//			snapshotter.setTxManager(transactionManagers.get());
		//		}

		if (snapshotter instanceof AggregateSnapshotter) {
			AggregateSnapshotter aggregateSnapshotter = (AggregateSnapshotter) snapshotter;
			registerAggregateFactories(aggregateSnapshotter);
		}
		return snapshotter;
	}

	//	private SnapshotEventStore eventStore(){
	//		Bean<SnapshotEventStore> bean = resolve(SnapshotEventStore.class, getQualifiers());
	//		return bean == null ? null : CdiUtils.getReference(getBeanManager(), bean);
	//	}

	//	@SuppressWarnings("unchecked")
	//	private <T> Bean<T> resolve(Class<T> type, Annotation[] qualifiers) {
	//		BeanManager bm = getBeanManager();
	//		return (Bean<T>) bm.resolve(bm.getBeans(type, qualifiers));
	//	}

	@SuppressWarnings("unchecked")
	private <T extends EventSourcedAggregateRoot<?>> void registerAggregateFactories(
			AggregateSnapshotter snapshotter) {
		Set<Annotation> qualifiers = ImmutableSet.copyOf(getQualifiers());
		List<AggregateFactory<?>> factories = Lists.newArrayList();
		for (AggregateRootInfo aggregateRoot : aggregateRoots) {
			if (aggregateRoot.matchQualifiers(QualifierType.SNAPSHOTTER, qualifiers)) {
				Set<Annotation> factoryQualifiers = aggregateRoot
						.getQualifiers(QualifierType.REPOSITORY);
				Type type = TypeUtils.parameterize(EventSourcingRepository.class,
						aggregateRoot.getType());
				EventSourcingRepository<T> repository = (EventSourcingRepository<T>) CdiUtils
						.getReference(getBeanManager(), type, factoryQualifiers);
				if (repository != null) {
					factories.add(repository.getAggregateFactory());
				}
			}
		}
		snapshotter.setAggregateFactories(factories);
	}
}
