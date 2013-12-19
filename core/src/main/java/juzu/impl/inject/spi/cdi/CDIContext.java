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

import juzu.impl.inject.ScopeController;
import juzu.impl.plugin.controller.AmbiguousResolutionException;
import juzu.impl.common.Filter;
import juzu.impl.inject.spi.InjectionContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CDIContext extends InjectionContext<Bean<?>, CreationalContext<?>> {

  /** . */
  public static final ThreadLocal<CDIContext> boot = new ThreadLocal<CDIContext>();

  /** . */
  final CDIInjector injector;

  /** . */
  final ArrayList<Bean> beans;

  /** . */
  final Filter<Class<?>, Boolean> filter;

  public CDIContext(CDIInjector injector, Filter<Class<?>, Boolean> filter) throws Exception {
    this.beans = new ArrayList<Bean>();
    this.filter = filter;

    //
    this.injector = injector;
  }

  @Override
  public ScopeController getScopeController() {
    return injector.scopeController;
  }

  public abstract ClassLoader getClassLoader();

  public abstract BeanManager getBeanManager();

  public Bean<?> resolveBean(Class<?> type) {
    BeanManager manager = getBeanManager();
    Set<Bean<?>> beans = manager.getBeans(type);
    switch (beans.size()) {
      case 0:
        return null;
      case 1:
        return beans.iterator().next();
      default:
        throw new AmbiguousResolutionException("Could not resolve bean of type " + type + ": " + beans);
    }
  }

  public Iterable<Bean<?>> resolveBeans(Class<?> type) {
    List<Bean<?>> resolved = Collections.emptyList();
    for (Bean bean : beans) {
      if (type.isAssignableFrom(bean.getBeanClass())) {
        if (resolved.isEmpty()) {
          resolved = new ArrayList<Bean<?>>();
        }
        resolved.add(bean);
      }
    }
    BeanManager manager = getBeanManager();
    return manager.getBeans(type);
  }

  public Bean<?> resolveBean(String name) {
    BeanManager manager = getBeanManager();
    Set<Bean<?>> beans = manager.getBeans(name);
    switch (beans.size()) {
      case 0:
        return null;
      case 1:
        return beans.iterator().next();
      default:
        throw new AmbiguousResolutionException("Could not resolve bean of type " + name + ": " + beans);
    }
  }

  public CreationalContext<?> createContext(Bean<?> bean) {
    BeanManager manager = getBeanManager();
    return manager.createCreationalContext(bean);
  }

  public void releaseContext(Bean<?> bean, CreationalContext<?> context) {
    context.release();
  }

  public Object getInstance(Bean<?> bean, CreationalContext<?> context) throws InvocationTargetException {
    try {
      BeanManager manager = getBeanManager();
      return manager.getReference(bean, bean.getBeanClass(), context);
    }
    catch (CreationException e) {
      e.printStackTrace();
      throw new InvocationTargetException(e.getCause());
    }
    catch (RuntimeException e) {
      throw new InvocationTargetException(e);
    }
  }

  public void close() {
  }
}
