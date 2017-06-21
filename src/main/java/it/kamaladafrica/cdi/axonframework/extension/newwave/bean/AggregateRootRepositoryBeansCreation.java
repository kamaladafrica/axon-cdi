package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.config.Configuration;

import com.google.common.base.Strings;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;
import it.kamaladafrica.cdi.axonframework.support.CdiUtils;

public class AggregateRootRepositoryBeansCreation extends AbstractBeanCreation {

	public AggregateRootRepositoryBeansCreation(final BeanCreation original) {
		super(original);
	}

	@Override
	protected Bean<?> concreateCreateBean(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo, final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Objects.requireNonNull(configuration);
		Bean<?> aggregateRootRepositoryBean = createRepositoryBean(beanManager, aggregateRootBeanInfo, configuration);
		return aggregateRootRepositoryBean;
	}

	@SuppressWarnings("unchecked")
	private <T> Bean<Repository<T>> createRepositoryBean(
			final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo, final Configuration configuration) {

		BeanBuilder<T> aggregateRootBean = new BeanBuilder<T>(beanManager)
				.readFromType((AnnotatedType<T>) aggregateRootBeanInfo.annotatedType());

		ParameterizedType repositoryType = TypeUtils.parameterize(Repository.class,
				aggregateRootBeanInfo.type());

		BeanBuilder<Repository<T>> builderRepository = new BeanBuilder<Repository<T>>(
				beanManager)
						.beanClass(Repository.class)
						.qualifiers(aggregateRootBeanInfo.qualifiers(QualifierType.DEFAULT))
						.alternative(aggregateRootBean.isAlternative())
						.nullable(aggregateRootBean.isNullable())
						.types(CdiUtils.typeClosure(repositoryType))
						.scope(aggregateRootBean.getScope())
						.stereotypes(aggregateRootBean.getStereotypes())
						.beanLifecycle(new RepositoryContextualLifecycle<>(aggregateRootBeanInfo, configuration));

		if (!Strings.isNullOrEmpty(aggregateRootBean.getName())) {
			builderRepository.name(aggregateRootBean.getName() + "Repository");
		}
		Bean<Repository<T>> repositoryBean = builderRepository.create();
		return repositoryBean;
	}

	// cf AggregateConfigurer
	private class RepositoryContextualLifecycle<T, R extends Repository<T>>
			implements ContextualLifecycle<R> {

		private final AggregateRootBeanInfo aggregateRootBeanInfo;

		private final Configuration configuration;

		public RepositoryContextualLifecycle(final AggregateRootBeanInfo aggregateRootBeanInfo, final Configuration configuration) {
			this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		@SuppressWarnings("unchecked")
		public R create(final Bean<R> bean, final CreationalContext<R> creationalContext) {
//			cf. AggregatesCdiConfigurer
			return (R) configuration.repository(aggregateRootBeanInfo.type());
		}

		@Override
		public void destroy(final Bean<R> bean, final R instance, final CreationalContext<R> creationalContext) {
			creationalContext.release();
		}

	}

}
