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
