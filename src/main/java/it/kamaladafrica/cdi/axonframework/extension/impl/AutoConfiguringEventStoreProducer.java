package it.kamaladafrica.cdi.axonframework.extension.impl;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Producer;

import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.eventhandling.SimpleEventHandlerInvoker;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.saga.AnnotatedSagaManager;
import org.axonframework.eventhandling.saga.SagaRepository;
import org.axonframework.eventhandling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.inmemory.InMemorySagaStore;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.eventstore.EventStore;

import com.google.common.collect.ImmutableSet;

import it.kamaladafrica.cdi.axonframework.support.CdiResourceInjector;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

// configure beans used to listen for events. Target beans are EventHandlers and Sagas

public class AutoConfiguringEventStoreProducer<X extends EventStore> extends
		AbstractAutoConfiguringProducer<X> {

	private final Set<HandlerInfo> eventHandlersInfo;

	private final Set<SagaManagerInfo> sagaManagersInfo;

	public AutoConfiguringEventStoreProducer(final Producer<X> wrappedProducer,
			final AnnotatedMember<?> annotatedMember,
			final Set<HandlerInfo> eventHandlersInfo,
			final Set<SagaInfo> sagasInfo,
			final BeanManager beanManager) {
		super(wrappedProducer, annotatedMember, beanManager);
		this.eventHandlersInfo = eventHandlersInfo;
		this.sagaManagersInfo = SagaManagerInfo.from(sagasInfo);
	}

	@Override
	protected X configure(final X eventStore) {
		final Set<Annotation> qualifiers = ImmutableSet.copyOf(getQualifiers());
		TokenStore tokenStore = new InMemoryTokenStore();
		Bean<?> tokenStoreBean = CdiUtils.getBean(getBeanManager(), TokenStore.class, qualifiers);
		if (tokenStoreBean != null) {
			tokenStore = (TokenStore) CdiUtils.getReference(getBeanManager(), tokenStoreBean, TokenStore.class);
		}
		registerEventScheduler(eventStore);
		registerEventHandlers(eventStore, tokenStore, qualifiers);
		registerSagaManager(eventStore, tokenStore, qualifiers);
		return eventStore;
	}

	private void registerEventScheduler(final X eventStore) {
		new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(), eventStore);
	}

	// Cf. EventHandlingConfiguration
	private void registerEventHandlers(final X eventStore, final TokenStore tokenStore, Set<Annotation> qualifiers) {
		for (HandlerInfo eventHandlerInfo : eventHandlersInfo) {
			Bean<?> eventHandlerBean = CdiUtils.getBean(getBeanManager(), eventHandlerInfo.getType(), qualifiers);
			if (eventHandlerBean != null) {
				/**
				SubscribingEventProcessor subscribingEventProcessor = new SubscribingEventProcessor(eventHandlerBean.getName() + "SubscribingEventProcessor",
						new SimpleEventHandlerInvoker(CdiUtils.getReference(getBeanManager(), eventHandlerBean, eventHandlerInfo.getType())),
						eventStore);
				subscribingEventProcessor.start();
				*/
				TrackingEventProcessor processor = new TrackingEventProcessor(eventHandlerBean.getName() + "Tracking",
						new SimpleEventHandlerInvoker(CdiUtils.getReference(getBeanManager(), eventHandlerBean, eventHandlerInfo.getType())),
						eventStore,
						tokenStore,
						NoTransactionManager.instance());
				processor.start();
			}
		}
	}

	// cf. SagaConfiguration
    @SuppressWarnings({"unchecked", "rawtypes"})
	private void registerSagaManager(final X eventStore, final TokenStore tokenStore, Set<Annotation> qualifiers) {
		SagaStore<?> sagaStore = new InMemorySagaStore();
		for (SagaManagerInfo sagaManagerInfo : sagaManagersInfo) {
			if (CdiUtils.qualifiersMatch(sagaManagerInfo.getEventBusQualifiers(), qualifiers)) {
				for (SagaInfo sagaInfo : sagaManagerInfo.getSagas()) {
					SagaRepository<?> sagaRepository = new AnnotatedSagaRepository(sagaInfo.getType(),
							sagaStore,
							new CdiResourceInjector(getBeanManager()));
					AnnotatedSagaManager<?> sagaManager = new AnnotatedSagaManager(sagaInfo.getType(),
							sagaRepository);
					TrackingEventProcessor processor = new TrackingEventProcessor(sagaInfo.getType().getSimpleName() + "Processor",
							sagaManager,
							eventStore,
							tokenStore,
							NoTransactionManager.instance());
					// TODO
//					processor.registerInterceptor(new CorrelationDataInterceptor<>(correlationDataProviders));
					processor.start();
				}
			}
		}
	}

}
