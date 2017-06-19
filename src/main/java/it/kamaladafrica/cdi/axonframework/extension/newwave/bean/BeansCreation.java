package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

public interface BeansCreation {

	void create(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, Set<Annotation> qualifiers, Configuration configuration);

}
