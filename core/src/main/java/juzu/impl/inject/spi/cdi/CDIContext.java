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

import juzu.impl.plugin.controller.AmbiguousResolutionException;
import juzu.impl.common.Filter;
import juzu.impl.inject.spi.InjectorProvider;
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
public class CDIContext extends InjectionContext<Bean<?>, CreationalContext<?>> {

  /** . */
  static final ThreadLocal<CDIContext> boot = new ThreadLocal<CDIContext>();

  /** . */
  private final Container container;

  /** . */
  private BeanManager manager;

  /** . */
  final ArrayList<AbstractBean> boundBeans;

  /** . */
  final ClassLoader classLoader;

  /** . */
  final ArrayList<Bean> beans;

  /** . */
  final Filter<Class<?>> filter;

  public CDIContext(
      Container container,
      Filter<Class<?>> filter,
      ArrayList<AbstractBean> boundBeans) throws Exception {
    this.boundBeans = boundBeans;
    this.beans = new ArrayList<Bean>();
    this.filter = filter;

    //
    boot.set(this);
    try {
      container.start();
    }
    finally {
      boot.set(null);
    }

    //
    this.classLoader = container.getClassLoader();
    this.manager = container.getManager();
    this.container = container;
  }

  public InjectorProvider getProvider() {
    return InjectorProvider.CDI_WELD;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Bean<?> resolveBean(Class<?> type) {
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
    for (int i = 0;i < beans.size();i++) {
      Bean bean = beans.get(i);
      if (type.isAssignableFrom(bean.getBeanClass())) {
        if (resolved.isEmpty()) {
          resolved = new ArrayList<Bean<?>>();
        }
        resolved.add(bean);
      }
    }
    return manager.getBeans(type);
  }

  public Bean<?> resolveBean(String name) {
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

  public CreationalContext<?> create(Bean<?> bean) {
    return manager.createCreationalContext(bean);
  }

  public void release(Bean<?> bean, CreationalContext<?> instance) {
    instance.release();
  }

  public Object get(Bean<?> bean, CreationalContext<?> instance) throws InvocationTargetException {
    try {
      return manager.getReference(bean, bean.getBeanClass(), instance);
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
    container.stop();
  }
}
