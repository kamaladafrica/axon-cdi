package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.Snapshotter;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.extension.impl.AggregateRootInfo.QualifierType;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class AutoConfiguringAggregateSnapshotterProducer<X extends Snapshotter> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<AggregateRootInfo> aggregateRootsInfo;

	public AutoConfiguringAggregateSnapshotterProducer(final Producer<X> wrappedProducer,
			final AnnotatedMember<?> annotatedMember, final Set<AggregateRootInfo> aggregateRoots,
			final BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.aggregateRootsInfo = aggregateRoots;
	}

	@Override
	protected X configure(final X snapshotter) {
		if (RegistrableAggregateSnaphotter.class.isAssignableFrom(snapshotter.getClass())) {
			RegistrableAggregateSnaphotter aggregateSnapshotter = (RegistrableAggregateSnaphotter) snapshotter;
			registerAggregateFactories(aggregateSnapshotter);
		}
		return snapshotter;
	}

	@SuppressWarnings("unchecked")
	private <T> void registerAggregateFactories(
			final RegistrableAggregateSnaphotter snapshotter) {
		Set<Annotation> qualifiers = ImmutableSet.copyOf(getQualifiers());
		for (AggregateRootInfo aggregateRoot : aggregateRootsInfo) {
			if (aggregateRoot.matchQualifiers(QualifierType.SNAPSHOTTER, qualifiers)) {
				Set<Annotation> factoryQualifiers = aggregateRoot
						.getQualifiers(QualifierType.REPOSITORY);
				Type type = TypeUtils.parameterize(EventSourcingRepository.class,
						aggregateRoot.getType());
				EventSourcingRepository<T> repository = (EventSourcingRepository<T>) CdiUtils
						.getReference(getBeanManager(), type, factoryQualifiers);
				if (repository != null) {
					snapshotter.registerAggregateFactory(repository.getAggregateFactory());
				}
				new EventCountSnapshotTriggerDefinition(snapshotter, 10);
			}
		}
	}

}
