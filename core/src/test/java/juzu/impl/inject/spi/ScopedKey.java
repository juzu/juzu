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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopedKey {

  /** . */
  private final Scope scope;

  /** . */
  private final Object scoped;

  ScopedKey(Scope scope, Object scoped) {
    this.scope = scope;
    this.scoped = scoped;
  }

  public Scope getScope() {
    return scope;
  }

  public Object getScoped() {
    return scoped;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ScopedKey) {
      ScopedKey that = (ScopedKey)obj;
      return scope == that.scope && scoped.equals(that.scoped);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return scope.hashCode() ^ scoped.hashCode();
  }
}
