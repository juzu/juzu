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

package juzu.impl.inject.spi.cdi;

import juzu.Scope;
import juzu.impl.inject.BeanFilter;
import juzu.impl.inject.ScopeController;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.cdi.weld.WeldContainer;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CDIBuilder extends InjectBuilder {

  /** . */
  private Set<Scope> scopes;

  /** . */
  private ClassLoader classLoader;

  /** . */
  private List<ReadFileSystem<?>> fileSystems;

  /** . */
  private ArrayList<AbstractBean> boundBeans;

  /** . */
  private BeanFilter filter;

  public CDIBuilder() {
    this.scopes = new HashSet<Scope>();
    this.fileSystems = new ArrayList<ReadFileSystem<?>>();
    this.boundBeans = new ArrayList<AbstractBean>();
    this.filter = null;
  }

  @Override
  public <T> InjectBuilder declareBean(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends T> implementationType) {
    boundBeans.add(new DeclaredBean(implementationType != null ? implementationType : type, beanScope, qualifiers));
    return this;
  }

  @Override
  public <T> InjectBuilder declareProvider(Class<T> type, Scope beanScope, Iterable<Annotation> qualifiers, Class<? extends Provider<T>> provider) {
    boundBeans.add(new DeclaredProviderBean(type, beanScope, qualifiers, provider));
    return this;
  }

  @Override
  public <P> InjectBuilder addFileSystem(ReadFileSystem<P> fs) {
    fileSystems.add(fs);
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
  public InjectBuilder setFilter(BeanFilter filter) {
    this.filter = filter;
    return this;
  }

  @Override
  public <T> InjectBuilder bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance) {
    boundBeans.add(new SingletonBean(type, qualifiers, instance));
    return this;
  }

  @Override
  public <T> InjectBuilder bindProvider(Class<T> beanType, Scope beanScope, Iterable<Annotation> beanQualifiers, Provider<T> provider) {
    boundBeans.add(new SingletonProviderBean(beanType, beanScope, beanQualifiers, provider));
    return this;
  }

  @Override
  public InjectionContext<?, ?> create() throws Exception {
    Container container = new WeldContainer(classLoader, ScopeController.INSTANCE, scopes);
    for (ReadFileSystem<?> fs : fileSystems) {
      container.addFileSystem(fs);
    }
    return new CDIContext(container, filter, boundBeans);
  }
}
