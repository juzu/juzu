/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.inject.spi.spring;

import juzu.Scope;
import juzu.impl.inject.BeanFilter;
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectionContext;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.io.UrlResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringBuilder extends InjectBuilder {

  /** . */
  private ClassLoader classLoader;

  /** . */
  private Map<String, AbstractBean> beans = new LinkedHashMap<String, AbstractBean>();

  /** The live instances. */
  final Map<String, Object> instances = new HashMap<String, Object>();

  /** . */
  private Set<Scope> scopes = new LinkedHashSet<Scope>();

  /** . */
  private URL configurationURL;

  /** . */
  final ScopeMetadataResolverImpl scopeResolver = new ScopeMetadataResolverImpl(scopes);

  public URL getConfigurationURL() {
    return configurationURL;
  }

  public void setConfigurationURL(URL configurationURL) {
    this.configurationURL = configurationURL;
  }

  @Override
  public InjectBuilder setFilter(BeanFilter filter) {
    return this;
  }

  public <T> InjectBuilder declareBean(AbstractBean bean) {
    String name = "" + Math.random();
    for (Annotation annotation : bean.type.getDeclaredAnnotations()) {
      if (annotation instanceof Named) {
        Named named = (Named)annotation;
        name = named.value();
        break;
      }
    }
    beans.put(name, bean);
    return this;
  }

  @Override
  public <T> InjectBuilder declareBean(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends T> implementationType) {
    if (implementationType == null) {
      implementationType = type;
    }
    return declareBean(new DeclaredBean(implementationType, beanScope, qualifiers));
  }

  @Override
  public <T> InjectBuilder bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance) {
    return declareBean(new SingletonBean(instance, qualifiers));
  }

  @Override
  public <T> InjectBuilder bindProvider(Class<T> beanType, Scope beanScope, Iterable<Annotation> beanQualifiers, final Provider<T> provider) {
    return declareBean(new SingletonProviderBean(beanType, beanScope, beanQualifiers, provider));
  }

  @Override
  public <T> InjectBuilder declareProvider(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends Provider<T>> provider) {
    return declareBean(new DeclaredProviderBean(type, beanScope, qualifiers, provider));
  }

  @Override
  public <P> InjectBuilder addFileSystem(ReadFileSystem<P> fs) {
    return this;
  }

  @Override
  public InjectBuilder addScope(Scope scope) {
    scopes.add(scope);
    return this;
  }

  @Override
  public InjectBuilder setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public <B, I> InjectionContext<B, I> create() throws Exception {
    DefaultListableBeanFactory factory;
    if (configurationURL != null) {
      factory = new XmlBeanFactory(new UrlResource(configurationURL));
    }
    else {
      factory = new DefaultListableBeanFactory();
    }

    //
    factory.setBeanClassLoader(classLoader);
    factory.setInstantiationStrategy(new SingletonInstantiationStrategy(new CglibSubclassingInstantiationStrategy(), instances));

    // Register scopes
    for (Scope scope : scopes) {
      if (!scope.isBuiltIn()) {
        factory.registerScope(scope.name().toLowerCase(), new SpringScope(factory, scope, ScopeController.INSTANCE));
      }
    }

    //
    for (Map.Entry<String, AbstractBean> entry : beans.entrySet()) {
      AbstractBean bean = entry.getValue();
      String name = entry.getKey();
      bean.configure(name, this, factory);
    }

    //
    AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
    beanPostProcessor.setAutowiredAnnotationType(Inject.class);
    beanPostProcessor.setBeanFactory(factory);
    factory.addBeanPostProcessor(beanPostProcessor);

    //
    CommonAnnotationBeanPostProcessor commonAnnotationBeanProcessor = new CommonAnnotationBeanPostProcessor();
    factory.addBeanPostProcessor(commonAnnotationBeanProcessor);

    //
    Set cqt = new HashSet();
    cqt.add(Named.class);
    CustomAutowireConfigurer configurer = new CustomAutowireConfigurer();
    configurer.setCustomQualifierTypes(cqt);
    QualifierAnnotationAutowireCandidateResolver customResolver = new QualifierAnnotationAutowireCandidateResolver();
    factory.setAutowireCandidateResolver(customResolver);
    configurer.postProcessBeanFactory(factory);

    //
    return (InjectionContext<B, I>)new SpringContext(factory, classLoader);
  }
}
