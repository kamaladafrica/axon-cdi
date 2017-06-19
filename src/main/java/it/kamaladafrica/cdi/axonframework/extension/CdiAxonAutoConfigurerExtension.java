package it.kamaladafrica.cdi.axonframework.extension;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.Aggregate;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.saga.ResourceInjector;
import org.axonframework.eventhandling.saga.Saga;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import it.kamaladafrica.cdi.axonframework.AutoConfigure;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.AggregateRootRepositoriesBeansCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.BeansCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.BeansCreationEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.CommandGatewayBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.EventSchedulerBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.AggregatesCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.AxonCdiConfigurationEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.AxonCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.CommandBusCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.CommandHandlersCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.CorrelationDataProviderCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.EventBusCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.EventHandlersCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.EventStorageEngineCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.ParameterResolverCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.PlatformTransactionManagerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.ResourceInjectorCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.SagaConfigurationsCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.SerializerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.TokenStoreCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.TransactionManagerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CommandGatewayProvidedInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CommandHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CorrelationDataProvidedInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.EventHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.SagaBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

/**
 * Original: SpringAxonAutoConfigurer
 */
/**
 * ImportBeanDefinitionRegistrar implementation that sets up an infrastructure Configuration based on beans available
 * in the application context.
 * <p>
 * This component is backed by a DefaultConfiguration (see {@link DefaultConfigurer#defaultConfiguration()}
 * and registers the following beans if present in the ApplicationContext:
 * <ul>
 * <li>{@link CommandBus}</li>
 * <li>{@link EventStorageEngine} or {@link EventBus}</li>
 * <li>{@link Serializer}</li>
 * <li>{@link TokenStore}</li>
 * <li>{@link PlatformTransactionManager}</li>
 * <li>{@link TransactionManager}</li>
 * <li>{@link SagaStore}</li>
 * <li>{@link ResourceInjector} (which defaults to {@link SpringResourceInjector}</li>
 * </ul>
 * <p>
 * Furthermore, all beans with an {@link Aggregate @Aggregate} or {@link Saga @Saga} annotation are inspected and
 * required components to operate the Aggregate or Saga are registered.
 *
 * @see EnableAxon
 */
public class CdiAxonAutoConfigurerExtension implements Extension {

	/**
	 * The entry point in an axon application is the command gateway
	 * So configurations are linked with command gateways
	 */

	private final Set<CommandGatewayProvidedInfo> commandGatewayProvidedInfos = Sets.newHashSet();

	private final Set<AggregateRootBeanInfo> aggregateRootBeanInfos = Sets.newHashSet();

	private final Set<SagaBeanInfo> sagaBeanInfos = Sets.newHashSet();

	private final Set<CorrelationDataProvidedInfo> correlationDataProvidedInfos = Sets.newHashSet();

	private final Set<CommandHandlerBeanInfo> commandHandlerBeanInfos = Sets.newHashSet(); 

	private final Set<EventHandlerBeanInfo> eventHandlerBeanInfos = Sets.newHashSet(); 

	//private final Set<Configurer> configurers = Sets.newHashSet();
	private final Set<Configuration> configurations = Sets.newHashSet();

	// producer
	<T, X extends CommandGateway> void processCommandGatewayProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();
		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			commandGatewayProvidedInfos.add(new CommandGatewayProvidedInfo(annotatedMember));
		}
	}

	<T, X extends CorrelationDataProvider> void processCorrelationDataProviderProducer(
			@Observes final ProcessProducer<T, X> processProducer,
			final BeanManager beanManager) {
		AnnotatedMember<T> annotatedMember = processProducer.getAnnotatedMember();
		if (annotatedMember.isAnnotationPresent(AutoConfigure.class)) {
			correlationDataProvidedInfos.add(new CorrelationDataProvidedInfo(annotatedMember));
		}
	}

	// lookup types bean
	<X> void processAggregateRootBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat,
			final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isAggregateRoot = AxonUtils.isAnnotatedAggregateRoot(at.getJavaClass());
		if (isAggregateRoot) {
			aggregateRootBeanInfos.add(new AggregateRootBeanInfo(at));
			pat.veto();
		}
	}

	<X> void processSagaBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		if (AxonUtils.isAnnotatedSaga(at.getJavaClass())) {
			sagaBeanInfos.add(new SagaBeanInfo(at));
			// pat.veto(); // don't veto this bean. Because we need it to discover EventScheduler injected beans
		}
	}

	<X> void processCommandsAndEventsHandlerBeanAnnotatedTypes(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> at = pat.getAnnotatedType();
		boolean isCommandHandler = AxonUtils.isCommandHandler(at.getJavaClass());
		boolean isEventHandler = AxonUtils.isEventHandler(at.getJavaClass());
		Preconditions.checkArgument(!isEventHandler || !isCommandHandler,
				"Provided type cannot be both event and command handler: %s", at);
		if (isCommandHandler) {
			commandHandlerBeanInfos.add(new CommandHandlerBeanInfo(at));
		} else if (isEventHandler) {
			eventHandlerBeanInfos.add(new EventHandlerBeanInfo(at));
		}
	}

	/**
	 * Create and add in CDI context axon injected objects
	 * @param afd
	 * @param bm
	 * @throws Exception 
	 */
	<T> void afterBeanDiscovery(@Observes final AfterBeanDiscovery afd, final BeanManager bm) throws Exception {
		for (CommandGatewayProvidedInfo commandGatewayProvidedInfo : commandGatewayProvidedInfos) {
			// create and setup configurer
			AxonCdiConfigurer axonCdiConfiguration =
				new EventHandlersCdiConfigurer(
					new CommandHandlersCdiConfigurer(
						new SagaConfigurationsCdiConfigurer(
							new AggregatesCdiConfigurer(
								new CorrelationDataProviderCdiConfigurer(
									new ResourceInjectorCdiConfigurer(
										new TransactionManagerCdiConfigurer(
											new PlatformTransactionManagerCdiConfigurer(
												new TokenStoreCdiConfigurer(
													new SerializerCdiConfigurer(
														new EventBusCdiConfigurer(
															new EventStorageEngineCdiConfigurer(
																new CommandBusCdiConfigurer(
																	new ParameterResolverCdiConfigurer(
																		new AxonCdiConfigurationEntryPoint()))))))))), correlationDataProvidedInfos), aggregateRootBeanInfos), sagaBeanInfos), commandHandlerBeanInfos), eventHandlerBeanInfos);
			Configurer configurer = axonCdiConfiguration.setUp(DefaultConfigurer.defaultConfiguration(), bm, commandGatewayProvidedInfo.normalizedQualifiers());
			Configuration configuration = configurer.buildConfiguration();
			// create cdi bean from configurer (repositories, event handlers, command handlers, event schedulers, command gateway)
			BeansCreation beansCreation =
				new CommandGatewayBeanCreation(
					new EventSchedulerBeanCreation(
						new AggregateRootRepositoriesBeansCreation(
							new BeansCreationEntryPoint(), aggregateRootBeanInfos)));
			beansCreation.create(afd, bm, commandGatewayProvidedInfo.normalizedQualifiers(), configuration);			
			configurations.add(configuration);
		}
	}

	/**
	 * 
	 * @param adv
	 * @param bm
	 */
	void afterDeploymentValidation(@Observes final AfterDeploymentValidation adv, final BeanManager bm) {
		configurations.stream().forEach(configuration -> configuration.start());
	}

}
