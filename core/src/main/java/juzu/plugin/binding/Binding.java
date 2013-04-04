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
