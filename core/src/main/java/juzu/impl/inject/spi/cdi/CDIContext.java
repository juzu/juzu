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

import juzu.AmbiguousResolutionException;
import juzu.impl.inject.BeanFilter;
import juzu.impl.inject.spi.InjectImplementation;
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
  final BeanFilter filter;

  public CDIContext(
      Container container,
      BeanFilter filter,
      ArrayList<AbstractBean> boundBeans) throws Exception {
    this.filter = filter;
    this.boundBeans = boundBeans;
    this.beans = new ArrayList<Bean>();

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

  public InjectImplementation getImplementation() {
    return InjectImplementation.CDI_WELD;
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

  public void shutdown() {
    container.stop();
  }
}
