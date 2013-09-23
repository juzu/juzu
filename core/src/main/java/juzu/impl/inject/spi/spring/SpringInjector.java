/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.inject.spi.spring;

import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectionContext;
import org.springframework.beans.factory.BeanFactory;
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
public class SpringInjector extends Injector {

  /** . */
  private ClassLoader classLoader;

  /** . */
  private LinkedHashMap<String, AbstractBean> beans;

  /** The live instances. */
  final HashMap<String, Object> instances;

  /** . */
  private final LinkedHashSet<Scope> scopes;

  /** . */
  private URL configurationURL;

  /** . */
  private BeanFactory parent;

  /** . */
  final ScopeMetadataResolverImpl scopeResolver;

  /** . */
  final ScopeController scopeController;

  public SpringInjector() {
    this.classLoader = null;
    this.beans = new LinkedHashMap<String, AbstractBean>();
    this.instances = new HashMap<String, Object>();
    this.scopes = new LinkedHashSet<Scope>();
    this.configurationURL = null;
    this.scopeResolver = new ScopeMetadataResolverImpl(scopes);
    this.scopeController = new ScopeController();
    this.parent = null;
  }

  private SpringInjector(SpringInjector that) {
    this.classLoader = that.classLoader;
    this.beans = new LinkedHashMap<String, AbstractBean>(that.beans);
    this.instances = new HashMap<String, Object>(that.instances);
    this.scopes = new LinkedHashSet<Scope>(that.scopes);
    this.configurationURL = that.configurationURL;
    this.scopeResolver = new ScopeMetadataResolverImpl(scopes);
    this.scopeController = new ScopeController();
    this.parent = that.parent;
  }

  public void setParent(Object parent) {
    this.parent = (BeanFactory)parent;
  }

  public <T> Injector declareBean(AbstractBean bean) {
    String name = Tools.nextUUID();
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
  public boolean isProvided() {
    return false;
  }

  @Override
  public <T> Injector declareBean(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends T> implementationType) {
    if (implementationType == null) {
      implementationType = type;
    }
    return declareBean(new DeclaredBean(implementationType, beanScope, qualifiers));
  }

  @Override
  public <T> Injector bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance) {
    return declareBean(new SingletonBean(instance, qualifiers));
  }

  @Override
  public <T> Injector bindProvider(Class<T> beanType, Scope beanScope, Iterable<Annotation> beanQualifiers, final Provider<? extends T> provider) {
    return declareBean(new SingletonProviderBean(beanType, beanScope, beanQualifiers, provider));
  }

  @Override
  public <T> Injector declareProvider(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends Provider<T>> provider) {
    return declareBean(new DeclaredProviderBean(type, beanScope, qualifiers, provider));
  }

  @Override
  public <P> Injector addFileSystem(ReadFileSystem<P> fs) {
    return this;
  }

  @Override
  public Injector addScope(Scope scope) {
    scopes.add(scope);
    return this;
  }

  @Override
  public Injector setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception {

    //
    DefaultListableBeanFactory factory;
    if (parent != null) {
      factory = new DefaultListableBeanFactory(parent);
    } else {
      if (configurationURL != null) {
        factory = new XmlBeanFactory(new UrlResource(configurationURL));
      }
      else {
        factory = new DefaultListableBeanFactory();
      }
    }

    //
    factory.setBeanClassLoader(classLoader);
    factory.setInstantiationStrategy(new SingletonInstantiationStrategy(new CglibSubclassingInstantiationStrategy(), instances));

    // Register scopes
    for (Scope scope : scopes) {
      if (!scope.isBuiltIn()) {
        factory.registerScope(scope.name().toLowerCase(), new SpringScope(factory, scope, scopeController));
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
    return new SpringContext(scopeController, factory, classLoader);
  }

  @Override
  public Injector get() {
    return new SpringInjector(this);
  }
}
