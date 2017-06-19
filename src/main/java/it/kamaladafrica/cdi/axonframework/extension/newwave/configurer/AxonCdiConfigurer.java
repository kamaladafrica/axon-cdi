package it.kamaladafrica.cdi.axonframework.extension.newwave.configurer;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

public interface AxonCdiConfigurer {

	Configurer setUp(Configurer configurer, BeanManager beanManager, Set<Annotation> qualifiers) throws Exception;

}
