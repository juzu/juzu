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

package juzu.impl.inject.spi.cdi;

import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.cdi.weld.WeldContainer;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CDIInjector extends Injector {

  /** . */
  private Set<Scope> scopes;

  /** . */
  private ClassLoader classLoader;

  /** . */
  private List<ReadFileSystem<?>> fileSystems;

  /** . */
  private ArrayList<AbstractBean> boundBeans;

  public CDIInjector() {
    this.scopes = new HashSet<Scope>();
    this.classLoader = null;
    this.fileSystems = new ArrayList<ReadFileSystem<?>>();
    this.boundBeans = new ArrayList<AbstractBean>();
  }

  public CDIInjector(CDIInjector that) {
    this.scopes = new HashSet<Scope>(that.scopes);
    this.classLoader = that.classLoader;
    this.fileSystems = new ArrayList<ReadFileSystem<?>>(that.fileSystems);
    this.boundBeans = new ArrayList<AbstractBean>(that.boundBeans);
  }

  @Override
  public <T> Injector declareBean(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends T> implementationType) {
    boundBeans.add(new DeclaredBean(implementationType != null ? implementationType : type, beanScope, qualifiers));
    return this;
  }

  @Override
  public <T> Injector declareProvider(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends Provider<T>> provider) {
    boundBeans.add(new DeclaredProviderBean(type, beanScope, qualifiers, provider));
    return this;
  }

  @Override
  public <P> Injector addFileSystem(ReadFileSystem<P> fs) {
    fileSystems.add(fs);
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
  public <T> Injector bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance) {
    boundBeans.add(new SingletonBean(type, qualifiers, instance));
    return this;
  }

  @Override
  public <T> Injector bindProvider(Class<T> beanType, Scope beanScope, Iterable<Annotation> beanQualifiers, Provider<? extends T> provider) {
    boundBeans.add(new SingletonProviderBean(beanType, beanScope, beanQualifiers, provider));
    return this;
  }

  @Override
  public InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception {
    Container container = new WeldContainer(classLoader, ScopeController.INSTANCE, scopes);
    for (ReadFileSystem<?> fs : fileSystems) {
      container.addFileSystem(fs);
    }
    return new CDIContext(container, filter, boundBeans);
  }

  @Override
  public Injector get() {
    return new CDIInjector(this);
  }
}
