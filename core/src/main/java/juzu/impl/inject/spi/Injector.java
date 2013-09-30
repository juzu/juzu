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

import juzu.Scope;
import juzu.impl.common.Filter;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder for configuring an {@link InjectionContext} implementation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Injector implements Provider<Injector> {

  private static final Filter<Class<?>> ALL = new Filter<Class<?>>() {
    public boolean accept(Class<?> elt) {
      return true;
    }
  };

  /**
   * Utils method that happens the qualifiers provided by the {@link Provider} <code>get</code> method to the list
   * of existing qualifiers
   *
   * @param qualifiers the original qualifiers
   * @param providerType the provided generic type
   * @return the annotations
   */
  public static Iterable<Annotation> appendProvidedQualifiers(Iterable<Annotation> qualifiers, Class<? extends Provider> providerType) {
    List<Annotation> next = null;
    try {
      Method getMethod = providerType.getMethod("get");
      for (Annotation annotation : getMethod.getAnnotations()) {
        if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
          if (next == null) {
            next = new ArrayList<Annotation>();
            if (qualifiers != null) {
              Tools.addAll(next, qualifiers);
            }
          }
          next.add(annotation);
        }
      }
    }
    catch (NoSuchMethodException e) {
      // ?
    }
    return next != null ? next : qualifiers;
  }

  public abstract boolean isProvided();

  /**
   * Declares a bean, bound to an optional implementation.
   *
   * @param beanType           the bean declared bean type
   * @param beanScope          the bean scope
   * @param beanQualifiers     the bean qualifiers
   * @param implementationType the bean implementation type
   * @return this builder
   */
  public abstract <T> Injector declareBean(
    Class<T> beanType,
    Scope beanScope,
    Iterable<Annotation> beanQualifiers,
    Class<? extends T> implementationType);

  /**
   * Declares a bean that will be obtained by the specified provider.
   *
   * @param beanType       the bean declared bean type
   * @param beanScope      the bean scope
   * @param beanQualifiers the bean qualifiers
   * @param providerType   the bean provider type
   * @return this builder
   */
  public abstract <T> Injector declareProvider(
    Class<T> beanType,
    Scope beanScope,
    Iterable<Annotation> beanQualifiers,
    Class<? extends Provider<T>> providerType);

  /**
   * Bind a bean type to a specified bean provider.
   *
   * @param beanType       the bean declared bean type
   * @param beanScope      the bean scope
   * @param beanQualifiers the bean qualifiers
   * @param provider       the bean provider
   * @return this builder
   */
  public abstract <T> Injector bindProvider(
    Class<T> beanType,
    Scope beanScope,
    Iterable<Annotation> beanQualifiers,
    Provider<? extends T> provider);

  /**
   * Bind a bean type to a specified bean instance.
   *
   * @param beanType       the bean declared bean type
   * @param beanQualifiers the bean qualifiers
   * @param instance       the bean instance
   * @return this builder
   */
  public abstract <T> Injector bindBean(
      Class<T> beanType,
      Iterable<Annotation> beanQualifiers,
      T instance);

  public abstract <P> Injector addFileSystem(ReadFileSystem<P> fs);

  public abstract Injector addScope(Scope scope);

  public abstract Injector setClassLoader(ClassLoader classLoader);

  public final InjectionContext<?, ?> create() throws Exception {
    return create(ALL);
  }

  public abstract InjectionContext<?, ?> create(Filter<Class<?>> filter) throws Exception;

  /**
   * Clone this injector.
   *
   * @return a close of this injector
   */
  public abstract Injector get();

}
