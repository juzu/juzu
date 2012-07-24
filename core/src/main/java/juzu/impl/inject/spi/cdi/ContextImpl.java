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
import juzu.impl.inject.ScopeController;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
final class ContextImpl implements Context {

  /** . */
  private final ScopeController controller;

  /** . */
  private final Class<? extends Annotation> scopeType;

  /** . */
  private final Scope scope;

  ContextImpl(ScopeController controller, Scope scope, Class<? extends Annotation> scopeType) {
    this.controller = controller;
    this.scopeType = scopeType;
    this.scope = scope;
  }

  public Class<? extends Annotation> getScope() {
    return scopeType;
  }

  public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
    try {
      CDIScoped<T> scoped = (CDIScoped<T>)controller.get(scope, contextual);
      if (scoped == null) {
        if (creationalContext != null) {
          T object = contextual.create(creationalContext);
          scoped = new CDIScoped<T>(contextual, creationalContext, object);
          controller.put(scope, contextual, scoped);
        }
      }
      return scoped != null ? scoped.object : null;
    }
    catch (IllegalStateException e) {
      throw new ContextNotActiveException("Context not active for scope=" + scope + " contextual=" + contextual, e);
    }
  }

  public <T> T get(Contextual<T> contextual) {
    return get(contextual, null);
  }

  public boolean isActive() {
    return controller.isActive(scope);
  }
}
