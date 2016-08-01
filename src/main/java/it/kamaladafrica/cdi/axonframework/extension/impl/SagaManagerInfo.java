package it.kamaladafrica.cdi.axonframework.extension.impl;

import it.kamaladafrica.cdi.axonframework.extension.impl.SagaInfo.QualifierType;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Multimap;

public final class SagaManagerInfo {

	private final Set<Annotation> repositoryQualifiers;

	private final Set<Annotation> factoryQualifiers;

	private final Set<Annotation> eventBusQualifiers;

	private final Set<SagaInfo> sagas;

	private SagaManagerInfo(Set<Annotation> repositoryQualifiers, Set<Annotation> factoryQualifiers,
			Set<Annotation> eventBusQualifiers, Set<SagaInfo> sagas) {
		Preconditions.checkArgument(!sagas.isEmpty());
		this.repositoryQualifiers = Objects.requireNonNull(repositoryQualifiers);
		this.factoryQualifiers = Objects.requireNonNull(factoryQualifiers);
		this.eventBusQualifiers = Objects.requireNonNull(eventBusQualifiers);
		this.sagas = sagas;
	}

	public Set<Annotation> getRepositoryQualifiers() {
		return repositoryQualifiers;
	}

	public Set<Annotation> getFactoryQualifiers() {
		return factoryQualifiers;
	}

	public Set<Annotation> getEventBusQualifiers() {
		return eventBusQualifiers;
	}

	public Set<SagaInfo> getSagas() {
		return sagas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((eventBusQualifiers == null) ? 0 : eventBusQualifiers.hashCode());
		result = prime * result + ((factoryQualifiers == null) ? 0 : factoryQualifiers.hashCode());
		result = prime * result
				+ ((repositoryQualifiers == null) ? 0 : repositoryQualifiers.hashCode());
		result = prime * result + ((sagas == null) ? 0 : sagas.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SagaManagerInfo other = (SagaManagerInfo) obj;
		if (eventBusQualifiers == null) {
			if (other.eventBusQualifiers != null) {
				return false;
			}
		} else if (!eventBusQualifiers.equals(other.eventBusQualifiers)) {
			return false;
		}
		if (factoryQualifiers == null) {
			if (other.factoryQualifiers != null) {
				return false;
			}
		} else if (!factoryQualifiers.equals(other.factoryQualifiers)) {
			return false;
		}
		if (repositoryQualifiers == null) {
			if (other.repositoryQualifiers != null) {
				return false;
			}
		} else if (!repositoryQualifiers.equals(other.repositoryQualifiers)) {
			return false;
		}
		if (sagas == null) {
			if (other.sagas != null) {
				return false;
			}
		} else if (!sagas.equals(other.sagas)) {
			return false;
		}
		return true;
	}

	public static Set<SagaManagerInfo> from(Iterable<SagaInfo> sagas) {
		Multimap<PartitionKey, SagaInfo> sagaInfos = HashMultimap.create();
		for (SagaInfo saga : sagas) {
			sagaInfos.put(PartitionKey.of(saga), saga);
		}

		Builder<SagaManagerInfo> builder = ImmutableSet.builder();
		for (Entry<PartitionKey, Collection<SagaInfo>> entry : sagaInfos.asMap().entrySet()) {
			PartitionKey key = entry.getKey();
			Set<SagaInfo> infos = ImmutableSet.copyOf(entry.getValue());
			builder.add(new SagaManagerInfo(key.getRepository(), key.getFactory(),
					key.getEventBus(), infos));
		}
		return builder.build();
	}

	private static final class PartitionKey {

		private final Set<Annotation> repository;

		private final Set<Annotation> factory;

		private final Set<Annotation> eventBus;

		private PartitionKey(Set<Annotation> repository, Set<Annotation> factory,
				Set<Annotation> eventBus) {
			this.repository = repository;
			this.factory = factory;
			this.eventBus = eventBus;
		}

		public Set<Annotation> getRepository() {
			return repository;
		}

		public Set<Annotation> getFactory() {
			return factory;
		}

		public Set<Annotation> getEventBus() {
			return eventBus;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eventBus == null) ? 0 : eventBus.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			result = prime * result + ((repository == null) ? 0 : repository.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			PartitionKey other = (PartitionKey) obj;
			if (eventBus == null) {
				if (other.eventBus != null) {
					return false;
				}
			} else if (!eventBus.equals(other.eventBus)) {
				return false;
			}
			if (factory == null) {
				if (other.factory != null) {
					return false;
				}
			} else if (!factory.equals(other.factory)) {
				return false;
			}
			if (repository == null) {
				if (other.repository != null) {
					return false;
				}
			} else if (!repository.equals(other.repository)) {
				return false;
			}
			return true;
		}

		static PartitionKey of(SagaInfo saga) {
			return new PartitionKey(saga.getQualifiers(QualifierType.REPOSITORY),
					saga.getQualifiers(QualifierType.FACTORY),
					saga.getQualifiers(QualifierType.EVENT_BUS));
		}

	}

}
