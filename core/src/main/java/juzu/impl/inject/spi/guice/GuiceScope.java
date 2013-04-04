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

package juzu.impl.inject.spi.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import juzu.impl.inject.ScopeController;

/**
 * Integrate guice scope.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class GuiceScope implements Scope {

  private final juzu.Scope scope;

  /** . */
  private final ScopeController controller;

  public GuiceScope(juzu.Scope scope, ScopeController controller) {
    this.scope = scope;
    this.controller = controller;
  }

  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return new Provider<T>() {
      public T get() {
        GuiceScoped scoped = (GuiceScoped)controller.get(scope, key);
        if (scoped == null) {
          scoped = new GuiceScoped(unscoped.get());
          controller.put(scope, key, scoped);
        }
        return (T)scoped.o;
      }
    };
  }
}
