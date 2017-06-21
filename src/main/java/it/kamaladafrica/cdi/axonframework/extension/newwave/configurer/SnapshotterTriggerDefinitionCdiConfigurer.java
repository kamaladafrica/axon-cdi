package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

/**
 * Used by AggregateConfigurer 
 * cf. 
 * snapshotTriggerDefinition = new Component<>(() -> parent, name("snapshotTriggerDefinition"),
 *                                                  c -> NoSnapshotTriggerDefinition.INSTANCE);
 * => Register as a component
 * @author damien
 *
 */
public class SnapshotterTriggerDefinitionCdiConfigurer extends AbstractCdiConfiguration {

	public SnapshotterTriggerDefinitionCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo)
			throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Bean<?> bean = aggregateRootBeanInfo.resolveBean(beanManager, QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION);
		if (bean != null) {
			SnapshotTriggerDefinition snapshotTriggerDefinition = (SnapshotTriggerDefinition) Proxy.newProxyInstance(
					SnapshotTriggerDefinition.class.getClassLoader(),
					new Class[] { SnapshotTriggerDefinition.class },
					new SnapshotTriggerDefinitionInvocationHandler(beanManager, aggregateRootBeanInfo));
			configurer.registerComponent(SnapshotTriggerDefinition.class, c -> snapshotTriggerDefinition);
		}
	}

	private class SnapshotTriggerDefinitionInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final AggregateRootBeanInfo aggregateRootBeanInfo;
		private SnapshotTriggerDefinition snapshotTriggerDefinition;

		public SnapshotTriggerDefinitionInvocationHandler(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (snapshotTriggerDefinition == null) {
				snapshotTriggerDefinition = (SnapshotTriggerDefinition) aggregateRootBeanInfo.getReference(beanManager, QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION);
			}
			return method.invoke(snapshotTriggerDefinition, args);
		}

	}

}
