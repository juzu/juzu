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

package juzu.impl.inject.spi.guice;

import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectionContext;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GuiceInjector extends Injector {

  /** . */
  final List<BeanBinding> bindings;

  /** . */
  final Set<Scope> scopes;

  /** . */
  ClassLoader classLoader;

  public GuiceInjector() {
    this.bindings = new ArrayList<BeanBinding>();
    this.scopes = new HashSet<Scope>();
  }

  public GuiceInjector(GuiceInjector that) {
    this.bindings = new ArrayList<BeanBinding>(that.bindings);
    this.scopes = new HashSet<Scope>(that.scopes);
  }

  @Override
  public boolean isProvided() {
    return false;
  }

  @Override
  public <T> Injector declareBean(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends T> implementationType) {
    bindings.add(new BeanBinding.ToType<T>(type, beanScope, qualifiers, implementationType));
    return this;
  }

  @Override
  public <T> Injector declareProvider(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends Provider<T>> provider) {
    bindings.add(new BeanBinding.ToProviderType<T>(type, beanScope, qualifiers, provider));
    return this;
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
  public <T> Injector bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance) {
    bindings.add(new BeanBinding.ToInstance<T>(type, qualifiers, instance));
    return this;
  }

  @Override
  public <T> Injector bindProvider(Class<T> beanType, Scope beanScope, Iterable<Annotation> beanQualifiers, Provider<? extends T> provider) {
    bindings.add(new BeanBinding.ToProviderInstance<T>(beanType, beanScope, beanQualifiers, provider));
    return this;
  }

  @Override
  public InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception {
    return new GuiceContext(this);
  }

  @Override
  public Injector get() {
    return new GuiceInjector(this);
  }
}
