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

import juzu.Scope;
import juzu.impl.inject.BeanFilter;
import juzu.impl.fs.spi.ReadFileSystem;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * A builder for configuring an {@link InjectionContext} implementation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class InjectBuilder {

  /**
   * Declares a bean, bound to an optional implementation.
   *
   * @param beanType           the bean declared bean type
   * @param beanScope          the bean scope
   * @param beanQualifiers     the bean qualifiers
   * @param implementationType the bean implementation type
   * @return this builder
   */
  public abstract <T> InjectBuilder declareBean(
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
  public abstract <T> InjectBuilder declareProvider(
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
  public abstract <T> InjectBuilder bindProvider(
    Class<T> beanType,
    Scope beanScope,
    Iterable<Annotation> beanQualifiers,
    Provider<T> provider);

  /**
   * Bind a bean type to a specified bean instance.
   *
   * @param beanType       the bean declared bean type
   * @param beanQualifiers the bean qualifiers
   * @param instance       the bean instance
   * @return this builder
   */
  public abstract <T> InjectBuilder bindBean(Class<T> beanType, Iterable<Annotation> beanQualifiers, T instance);

  public abstract <P> InjectBuilder addFileSystem(ReadFileSystem<P> fs);

  public abstract InjectBuilder addScope(Scope scope);

  public abstract InjectBuilder setClassLoader(ClassLoader classLoader);

  public abstract InjectBuilder setFilter(BeanFilter filter);

  public abstract <B, I> InjectionContext<B, I> create() throws Exception;

}
