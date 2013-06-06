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

package juzu.impl.inject.spi;

import juzu.impl.inject.ScopeController;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class InjectionContext<B, I> implements Closeable {

  public abstract ScopeController getScopeController();

  /**
   * Returns the injector provider.
   *
   * @return the injector provider
   */
  public abstract InjectorProvider getProvider();

  public abstract ClassLoader getClassLoader();

  public abstract B resolveBean(Class<?> type);

  public abstract B resolveBean(String name);

  public abstract Iterable<B> resolveBeans(Class<?> type);

  /**
   * Create a bean instance for the specified bean.
   *
   * @param bean the bean
   * @return the bean instance
   * @throws InvocationTargetException wrap any exception throws,by the bean class during its creation.
   */
  public abstract I create(B bean) throws InvocationTargetException;

  /**
   * Get the bean object associated the bean instance.
   *
   * @param bean     the bean
   * @param instance the bean instance
   * @return the bean instance
   * @throws InvocationTargetException wrap any exception throws,by the bean class during its creation.
   */
  public abstract Object get(B bean, I instance) throws InvocationTargetException;

  public abstract void release(B bean, I instance);

  /**
   * Close the manager. The implementation should care bout shutting down the existing bean in particular the
   * singleton beans that are managed outside of an explicit scope.
   */
  public abstract void close();

  private static class BeanLifeCycleImpl<B,I,T> implements BeanLifeCycle<T> {

    final Class<T> type;
    final InjectionContext<B, I> manager;
    final B a;
    private I instance;
    private T o;

    private BeanLifeCycleImpl(Class<T> type, InjectionContext<B, I> manager, B a) {
      this.type = type;
      this.manager = manager;
      this.a = a;
    }

    public T get() throws InvocationTargetException {
      if (o == null) {
        instance = manager.create(a);
        o = type.cast(manager.get(a, instance));
      }
      return o;
    }

    public T peek() {
      return o;
    }

    public void close() {
      if (instance != null) {
        manager.release(a, instance);
      }
    }
  }

  public final <T> BeanLifeCycle<T> get(Class<T> type) {
    final B a = resolveBean(type);
    if (a == null) {
      return null;
    } else {
      return new BeanLifeCycleImpl<B,I,T>(type, this, a);
    }
  }

  public final <T> Iterable<BeanLifeCycle<T>> resolve(final Class<T> type) {
    final Iterable<B> a = resolveBeans(type);
    return new Iterable<BeanLifeCycle<T>>() {
      public Iterator<BeanLifeCycle<T>> iterator() {
        return new Iterator<BeanLifeCycle<T>>() {
          final Iterator<B> i = a.iterator();
          public boolean hasNext() {
            return i.hasNext();
          }
          public BeanLifeCycle<T> next() {
            B b = i.next();
            return new BeanLifeCycleImpl<B,I,T>(type, InjectionContext.this, b);
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public final <T> T resolveInstance(Class<T> beanType) {
    try {
      BeanLifeCycle<T> pluginLifeCycle = get(beanType);
      return pluginLifeCycle != null ? pluginLifeCycle.get() : null;
    }
    catch (InvocationTargetException e) {
      // log.log("Could not retrieve bean of type " + beanType, e.getCause());
      return null;
    }
  }

  public final <T> Iterable<T> resolveInstances(final Class<T> beanType) {
    return new Iterable<T>() {
      Iterable<BeanLifeCycle<T>> lifecycles = resolve(beanType);
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<BeanLifeCycle<T>> iterator = lifecycles.iterator();
          T next = null;
          public boolean hasNext() {
            while (next == null && iterator.hasNext()) {
              try {
                BeanLifeCycle<T> pluginLifeCycle = iterator.next();
                next = pluginLifeCycle.get();
              }
              catch (InvocationTargetException e) {
                // log.log("Could not retrieve bean of type " + beanType.getName(), e);
              }
            }
            return next != null;
          }
          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            } else {
              T tmp = next;
              next = null;
              return tmp;
            }
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
