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

package juzu.impl.inject;

import juzu.Scope;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeController {

  /** . */
  final ThreadLocal<ScopingContext> currentContext = new ThreadLocal<ScopingContext>();

  public ScopeController() {
  }

  public void begin(ScopingContext context) throws IllegalStateException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (currentContext.get() != null) {
      throw new IllegalStateException("Already started");
    }
    currentContext.set(context);
  }

  public void end() {
    currentContext.set(null);
  }

  /**
   * Obtain a scoped object.
   *
   * @param scope the scope
   * @param key   the key
   * @return the scoped object or null
   * @throws IllegalStateException if the scope is not active
   */
  public Scoped get(Scope scope, Object key) throws IllegalStateException {
    ScopingContext ctx = currentContext.get();
    if (ctx == null) {
      throw new IllegalStateException("Context not active");
    }
    if (!ctx.isActive(scope)) {
      throw new IllegalStateException("Context not active");
    }
    return ctx.getContextualValue(scope, key);
  }

  /**
   * Scope an object.
   *
   * @param scope  the scope
   * @param key    the key
   * @param scoped
   * @throws IllegalStateException if the scope is not active
   */
  public void put(Scope scope, Object key, Scoped scoped) throws IllegalStateException {
    ScopingContext ctx = currentContext.get();
    if (ctx == null) {
      throw new IllegalStateException("Context not active");
    }
    if (!ctx.isActive(scope)) {
      throw new IllegalStateException("Context not active");
    }
    ctx.setContextualValue(scope, key, scoped);
  }

  /**
   * Tells if a scope is active or not.
   *
   * @param scope the scope
   * @return true if the scope is active
   */
  public boolean isActive(Scope scope) {
    ScopingContext ctx = currentContext.get();
    return ctx != null && ctx.isActive(scope);
  }
}
