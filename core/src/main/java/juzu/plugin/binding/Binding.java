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

package juzu.plugin.binding;

import juzu.Scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a bean binding, i.e an external bean that is not managed by Juzu. This bean will be managed by the
 * dependency injection container.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Binding {

  /**
   * The bean class that will be made available for injection by the container.
   *
   * @return the bean class
   */
  Class<?> value();

  /**
   * The scope for which the bean will be bound.
   *
   * @return the bean scope
   */
  Scope scope() default Scope.SINGLETON;

  /**
   * The optional bean implementation class, when this class is provided it should satisfy one condition among those:
   * <p/>
   * <ul> <li>Extend or implement the {@link #value()} bean class</li> <li>Implement the {@link javax.inject.Provider}
   * interface with a generic type <code>&lt;T&gt;</code> that must be a sub type of the {@link #value()} class</li>
   * <li>Implement the {@link juzu.inject.ProviderFactory} interface</li> </ul>
   *
   * @return the bean implementation
   */
  Class<?> implementation() default Object.class;

}
