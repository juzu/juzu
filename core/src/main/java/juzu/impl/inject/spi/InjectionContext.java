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

package juzu.impl.inject.spi;

import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class InjectionContext<B, I> {

  /**
   * Returns the implementation.
   *
   * @return the implementation
   */
  public abstract InjectImplementation getImplementation();

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
   * Shutdown the manager. The implementation should care bout shutting down the existing bean in particular the
   * singleton beans that are managed outside of an explicit scope.
   */
  public abstract void shutdown();

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

    public void release() {
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
}
