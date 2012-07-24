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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SingletonBean extends AbstractSingletonBean {

  /** . */
  private final Object instance;

  /** . */
  private final Lock lock;

  /** . */
  private boolean initialized;

  /** . */
  private AnnotatedType at;

  /** . */
  private InjectionTarget it;

  SingletonBean(Class type, Iterable<Annotation> qualifiers, Object instance) {
    super(type, Scope.SINGLETON, qualifiers);

    //
    this.instance = instance;
    this.initialized = false;
    this.lock = new ReentrantLock();
  }

  @Override
  void register(BeanManager manager) {
    super.register(manager);

    //
    this.at = manager.createAnnotatedType(instance.getClass());
    this.it = manager.createInjectionTarget(at);
  }

  public Object create(CreationalContext creationalContext) {
    lock.lock();
    try {
      if (!initialized) {
        if (it != null) {
          it.inject(instance, creationalContext);
          it.postConstruct(instance);
        }
        initialized = true;
      }
    } finally {
      lock.unlock();
    }
    return instance;
  }

  @Override
  public void destroy(Object instance, CreationalContext ctx) {
    lock.lock();
    try {
      if (initialized) {
        if (it != null) {
          it.preDestroy(instance);
          it.dispose(instance);
        }
        initialized = false;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    return "SingletonBean[type=" + type + ",qualifiers=" + qualifiers + "]";
  }
}
