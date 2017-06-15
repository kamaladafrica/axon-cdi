package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.axonframework.eventsourcing.eventstore.EventStore;

import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class EventSchedulerInfo {

	private final Type type;

	private final Set<Annotation> qualifiers;

	private EventSchedulerInfo(final Type type, final Set<Annotation> qualifiers) {
		this.type = type;
		this.qualifiers = CdiUtils.normalizedQualifiers(qualifiers);
	}

	public Type getType() {
		return type;
	}

	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	public EventStore getEventStoreReference(final BeanManager bm) {
		return (EventStore) CdiUtils.getReference(bm, EventStore.class, qualifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		EventSchedulerInfo other = (EventSchedulerInfo) obj;
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

	public static EventSchedulerInfo of(final InjectionPoint injectionPoint) {
		return new EventSchedulerInfo(injectionPoint.getType(),
				injectionPoint.getQualifiers());
	}

}
