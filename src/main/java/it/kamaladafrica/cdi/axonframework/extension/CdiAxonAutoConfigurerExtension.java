package it.kamaladafrica.cdi.axonframework.extension;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.AggregateRootRepositoryBeansCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.BeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.BeansCreationEntryPoint;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.CommandGatewayBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.EventSchedulerBeanCreation;
import it.kamaladafrica.cdi.axonframework.extension.newwave.bean.SnapshotterBeanCreation;
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
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.SnapshotterTriggerDefinitionCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.TokenStoreCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.configurer.TransactionManagerCdiConfigurer;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.CommandHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.EventHandlerBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.SagaBeanInfo;
import it.kamaladafrica.cdi.axonframework.support.AxonUtils;

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

	/**
	 * The entry point in an axon application is defined on AggregateRoot using @AggregateConfiguration (or annotation using it) or not
	 * @AggregateConfiguration give special setup
	 * Loop throught AggregateRootBeanInfo to get data
	 */
	private final Set<AggregateRootBeanInfo> aggregateRootBeanInfos = Sets.newHashSet();

	private final Set<SagaBeanInfo> sagaBeanInfos = Sets.newHashSet();

	private final Set<CommandHandlerBeanInfo> commandHandlerBeanInfos = Sets.newHashSet(); 

	private final Set<EventHandlerBeanInfo> eventHandlerBeanInfos = Sets.newHashSet(); 

	//private final Set<Configurer> configurers = Sets.newHashSet();
	private final Set<Configuration> configurations = Sets.newHashSet();

	// lookup types bean
	<X> void processAggregateRootBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat,
			final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = pat.getAnnotatedType();
		boolean isAggregateRoot = AxonUtils.isAnnotatedAggregateRoot(annotatedType.getJavaClass());
		if (isAggregateRoot) {
			aggregateRootBeanInfos.add(AggregateRootBeanInfo.of(beanManager, annotatedType));
			pat.veto();
		}
	}

	<X> void processSagaBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> pat, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = pat.getAnnotatedType();
		if (AxonUtils.isAnnotatedSaga(annotatedType.getJavaClass())) {
			sagaBeanInfos.add(SagaBeanInfo.of(beanManager, annotatedType));
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
		LOGGER.log(Level.INFO, "Axon CDI Extension - Activated");
		for (AggregateRootBeanInfo aggregateRootBeanInfo : aggregateRootBeanInfos) {
			
//			TON CODE ne marche pas ...
//			Plusieurs aggregats peuvent partager la meme conf. IE le meme command bus
//			Du coup ma methode setUp doit prendre une liste d'aggregats !!!
//			Ayant les memes qualifiers !!! Sur le type desirée !!!
//			C'est chaud !!!
//			Je 
//			// Create and setup configurer
//			AxonCdiConfigurer axonCdiConfiguration =
//				new SnapshotterTriggerDefinitionCdiConfigurer(
//					new EventHandlersCdiConfigurer(
//						new CommandHandlersCdiConfigurer(
//							new SagaConfigurationsCdiConfigurer(
//								new AggregatesCdiConfigurer(
//									new CorrelationDataProviderCdiConfigurer(
//										new ResourceInjectorCdiConfigurer(
//											new TransactionManagerCdiConfigurer(
//												new PlatformTransactionManagerCdiConfigurer(
//													new TokenStoreCdiConfigurer(
//														new SerializerCdiConfigurer(
//															new EventBusCdiConfigurer(
//																new EventStorageEngineCdiConfigurer(
//																	new CommandBusCdiConfigurer(
//																		new ParameterResolverCdiConfigurer(
//																			new AxonCdiConfigurationEntryPoint()))))))))))), sagaBeanInfos), commandHandlerBeanInfos), eventHandlerBeanInfos));
//			Configurer configurer = axonCdiConfiguration.setUp(DefaultConfigurer.defaultConfiguration(), bm, aggregateRootBeanInfo);
//			// create *configuration* from previous setup configurer
//			Configuration configuration = configurer.buildConfiguration();
			// Create cdi bean from configuration (repositories, event handlers, command handlers, event schedulers, command gateway)
//			BeanCreation beansCreation =
//				new (
//					new CommandGatewayBeanCreation(
//						new EventSchedulerBeanCreation(
//							new AggregateRootRepositoryBeansCreation(
//									new BeansCreationEntryPoint()))));
//			beansCreation.create(afd, bm, aggregateRootBeanInfo, configuration);
//			configurations.add(configuration);
//			new SnapshotterBeanCreation(aggregateRootBeanInfo, configuration);PUTAIIN je suis nicker car je prends la conf en entrée !!!
		}
	}

	/**
	 * 
	 * @param adv
	 * @param bm
	 */
	void afterDeploymentValidation(@Observes final AfterDeploymentValidation adv, final BeanManager bm) {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Starting");
		configurations.stream().forEach(configuration -> configuration.start());
		LOGGER.log(Level.INFO, "Axon CDI Extension - Started");
	}

}
