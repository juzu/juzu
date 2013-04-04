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
