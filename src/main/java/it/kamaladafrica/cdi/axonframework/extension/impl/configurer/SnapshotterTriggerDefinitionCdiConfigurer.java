package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

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
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext)
			throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		SnapshotTriggerDefinition snapshotTriggerDefinition = (SnapshotTriggerDefinition) Proxy.newProxyInstance(
			SnapshotTriggerDefinition.class.getClassLoader(),
			new Class[] { SnapshotTriggerDefinition.class },
			new SnapshotTriggerDefinitionInvocationHandler(beanManager, executionContext));
		// only one can be registered per configurer
		configurer.registerComponent(SnapshotTriggerDefinition.class, c -> snapshotTriggerDefinition);
	}

	private class SnapshotTriggerDefinitionInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private SnapshotTriggerDefinition snapshotTriggerDefinition;

		public SnapshotTriggerDefinitionInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (snapshotTriggerDefinition == null) {
				snapshotTriggerDefinition = executionContext.getSnapshotTriggerDefinitionReference(beanManager);
			}
			return method.invoke(snapshotTriggerDefinition, args);
		}

	}

}
