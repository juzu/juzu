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
