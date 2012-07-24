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

package juzu.impl.inject;

import juzu.Scope;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeController {

  /** . */
  public static final ScopeController INSTANCE = new ScopeController();

  /** . */
  final ThreadLocal<ScopingContext> currentContext = new ThreadLocal<ScopingContext>();

  public static void begin(ScopingContext context) throws IllegalStateException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (INSTANCE.currentContext.get() != null) {
      throw new IllegalStateException("Already started");
    }
    INSTANCE.currentContext.set(context);
  }

  public static void end() {
    INSTANCE.currentContext.set(null);
  }

  private ScopeController() {
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
