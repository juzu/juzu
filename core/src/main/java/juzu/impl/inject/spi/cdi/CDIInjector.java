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
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.spi.Injector;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CDIInjector extends Injector {

  /** . */
  protected final Set<Scope> scopes;

  /** . */
  protected final ArrayList<AbstractBean> boundBeans;

  /** . */
  protected final ScopeController scopeController;

  public CDIInjector() {
    this.scopes = new HashSet<Scope>();
    this.boundBeans = new ArrayList<AbstractBean>();
    this.scopeController = new ScopeController();
  }

  public CDIInjector(CDIInjector that) {
    this.scopes = new HashSet<Scope>(that.scopes);
    this.boundBeans = new ArrayList<AbstractBean>(that.boundBeans);
    this.scopeController = that.scopeController;
  }

  public Set<Scope> getScopes() {
    return scopes;
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
  public Injector addScope(Scope scope) {
    scopes.add(scope);
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
}
