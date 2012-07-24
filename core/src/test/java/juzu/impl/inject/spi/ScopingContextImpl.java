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
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopingContextImpl implements ScopingContext {

  /** . */
  private final Map<ScopedKey, Scoped> entries = new HashMap<ScopedKey, Scoped>();

  public Scoped getContextualValue(Scope scope, Object key) {
    return entries.get(new ScopedKey(scope, key));
  }

  public void setContextualValue(Scope scope, Object key, Scoped value) {
    if (value != null) {
      entries.put(new ScopedKey(scope, key), value);
    }
    else {
      entries.remove(new ScopedKey(scope, key));
    }
  }

  public boolean isActive(Scope scope) {
    return true;
  }

  public Map<ScopedKey, Scoped> getEntries() {
    return entries;
  }
}
