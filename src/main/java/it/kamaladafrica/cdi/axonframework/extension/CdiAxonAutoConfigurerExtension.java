package it.kamaladafrica.cdi.axonframework.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;

import it.kamaladafrica.cdi.axonframework.extension.impl.bean.AggregatesRootRepositoriesBeansCreation;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.BeanCreationEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.BeansCreationHandler;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.CommandGatewayBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.EventSchedulerBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.MetricRegistryBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.SnapshotterBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation.ApplicationScopedBeanValidator;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation.BeanScopeNotValidException;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation.BeanScopeValidator;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation.BeanScopeValidatorEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.impl.bean.validation.DependentScopedBeanValidator;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.AggregatesCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.AxonCdiConfigurationEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.AxonCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.CommandBusCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.CommandHandlersCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.CorrelationDataProviderCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.EventBusCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.EventHandlersCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.EventStorageEngineCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.MetricRegistryCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.ParameterResolverCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.PlatformTransactionManagerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.ResourceInjectorCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.SagaConfigurationsCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.SerializerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.SnapshotterTriggerDefinitionCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.TokenStoreCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.configurer.TransactionManagerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.CommandHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.EventHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.SagaBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;
import it.kamaladafrica.cdi.axonframework.support.BeforeStartingAxon;

/**
 * Original: SpringAxonAutoConfigurer
 */
/**
 * TODO explain why bean reference are proxified
 * @author damien
 *
 */
public class CdiAxonAutoConfigurerExtension implements Extension {

	private static final Logger LOGGER = Logger.getLogger(CdiAxonAutoConfigurerExtension.class.getName());

	private final List<ExecutionContext> executionContexts = new ArrayList<>();

	private final List<SagaBeanInfo> sagaBeanInfos = new ArrayList<>();

	private final List<CommandHandlerBeanInfo> commandHandlerBeanInfos = new ArrayList<>();

	private final List<EventHandlerBeanInfo> eventHandlerBeanInfos = new ArrayList<>();

	private final List<Configuration> configurations = new ArrayList<>();

	// lookup types bean
	<X> void processAggregateRootBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType,
			final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		boolean isAggregateRoot = AxonUtils.isAnnotatedAggregateRoot(annotatedType.getJavaClass());
		if (isAggregateRoot) {
			AggregateRootBeanInfo aggregateRootBeanInfo = AggregateRootBeanInfo.of(beanManager, annotatedType);
			boolean hasRegisteredDiscoveredAggregateRootBean = false;
			for (ExecutionContext executionContext : executionContexts) {
				if (executionContext.registerIfSameContext(aggregateRootBeanInfo)) {
					hasRegisteredDiscoveredAggregateRootBean = true;
				}
			}
			if (!hasRegisteredDiscoveredAggregateRootBean) {
				executionContexts.add(new ExecutionContext(aggregateRootBeanInfo));
			}
			processAnnotatedType.veto();
		}
	}

	<X> void processSagaBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		if (AxonUtils.isAnnotatedSaga(annotatedType.getJavaClass())) {
			sagaBeanInfos.add(SagaBeanInfo.of(beanManager, annotatedType));
			// pat.veto(); // don't veto this bean. Because we need it to discover EventScheduler injected beans
		}
	}

	<X> void processCommandsAndEventsHandlerBeanAnnotatedTypes(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		boolean isCommandHandler = AxonUtils.isCommandHandlerBean(annotatedType.getJavaClass());
		boolean isEventHandler = AxonUtils.isEventHandlerBean(annotatedType.getJavaClass());
		Preconditions.checkArgument((isEventHandler && isCommandHandler) != true,
				"Provided type cannot be both event and command handler: %s", annotatedType);
		if (isCommandHandler) {
			commandHandlerBeanInfos.add(new CommandHandlerBeanInfo(annotatedType));
		} else if (isEventHandler) {
			eventHandlerBeanInfos.add(new EventHandlerBeanInfo(annotatedType));
		}
	}

	/**
	 * Create and add in CDI context axon injected objects
	 * @param afterBeanDiscovery
	 * @param beanManager
	 * @throws Exception 
	 */
	<T> void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) throws Exception {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Activated");
		// Scoped validations
		BeanScopeValidator beanScopeValidator = new ApplicationScopedBeanValidator(
			new ApplicationScopedBeanValidator(
				new ApplicationScopedBeanValidator(
					new ApplicationScopedBeanValidator(
						new ApplicationScopedBeanValidator(
							new ApplicationScopedBeanValidator(
								new ApplicationScopedBeanValidator(
									new ApplicationScopedBeanValidator(
										new ApplicationScopedBeanValidator(
											new ApplicationScopedBeanValidator(
												new ApplicationScopedBeanValidator(
													new BeanScopeValidatorEntryPoint(),
														CommandBus.class),
															CommandGateway.class),
																EventBus.class),
																	SnapshotTriggerDefinition.class),
																		TokenStore.class),
																			TransactionManager.class),
																				Serializer.class),
																					EventStorageEngine.class),
																						EventScheduler.class),
																							CorrelationDataProvider.class),
																								MetricRegistry.class); 
		;
		for (SagaBeanInfo sagaBeanInfo : sagaBeanInfos) {
			beanScopeValidator = new DependentScopedBeanValidator(beanScopeValidator, sagaBeanInfo.type());
		}
		for (CommandHandlerBeanInfo commandHandlerBeanInfo : commandHandlerBeanInfos) {
			beanScopeValidator = new DependentScopedBeanValidator(beanScopeValidator, commandHandlerBeanInfo.type());			
		}
		for (EventHandlerBeanInfo eventHandlerBeanInfo : eventHandlerBeanInfos) {
			beanScopeValidator = new DependentScopedBeanValidator(beanScopeValidator, eventHandlerBeanInfo.type());
		}
		try {
			beanScopeValidator.validate(beanManager);
		} catch (final BeanScopeNotValidException beanScopeNotValidException) {
			afterBeanDiscovery.addDefinitionError(
					new InjectionException(
						String.format("'%s' must be defined as '%s' to avoid clashes between instances",
							beanScopeNotValidException.bean().toString(),
							beanScopeNotValidException.expectedBeanScoped().getSimpleName())));
		}
		// Context assemblers
		sagaBeanInfos.forEach(sagaBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(sagaBeanInfo));
		});
		commandHandlerBeanInfos.forEach(commandHandlerBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(commandHandlerBeanInfo));
		});
		eventHandlerBeanInfos.forEach(eventHandlerBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(eventHandlerBeanInfo));
		});
		// Create and setup configurer for each context
		for (ExecutionContext executionContext : executionContexts) {
			AxonCdiConfigurer axonCdiConfiguration =
				new MetricRegistryCdiConfigurer(
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
																			new SnapshotterTriggerDefinitionCdiConfigurer(
																				new AxonCdiConfigurationEntryPoint()))))))))))))))));
			Configurer configurer = axonCdiConfiguration.setUp(DefaultConfigurer.defaultConfiguration(), beanManager, executionContext);
			// create *configuration* from previous setup configurer
			Configuration configuration = configurer.buildConfiguration();
			// Create cdi bean from configuration (repositories, event handlers, command handlers, event schedulers, command gateway)
			BeansCreationHandler beansCreation =
				new MetricRegistryBeanCreation(
					new SnapshotterBeanCreation(
						new CommandGatewayBeanCreation(
							new EventSchedulerBeanCreation(
								new AggregatesRootRepositoriesBeansCreation(
									new BeanCreationEntryPoint())))));
			beansCreation.create(afterBeanDiscovery, beanManager, executionContext, configuration);
			configurations.add(configuration);
		}
	}

	/**
	 * 
	 * @param afterDeploymentValidation
	 * @param beanManager
	 */
	void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation, final BeanManager beanManager) {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Starting");
		beanManager.fireEvent(new BeforeStartingAxon());
		configurations.stream().forEach(configuration -> configuration.start());
		LOGGER.log(Level.INFO, "Axon CDI Extension - Started");
	}

}
