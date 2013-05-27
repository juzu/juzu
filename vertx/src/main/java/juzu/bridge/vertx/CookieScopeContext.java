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
package juzu.bridge.vertx;

import com.google.inject.Key;
import juzu.impl.bridge.spi.ScopedContext;
import juzu.impl.common.Tools;
import juzu.impl.inject.Scoped;
import juzu.io.UndeclaredIOException;

import javax.inject.Named;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author Julien Viet */
public class CookieScopeContext implements ScopedContext {

  /** . */
  public static final int FLASH = 0;

  /** . */
  public static final int SESSION = 1;

  /** . */
  public static final int TO_SEND = 0;

  /** . */
  public static final int RECEIVED = 1;

  /** . */
  public static final int TO_REMOVE = 2;

  /** . */
  HashMap<String, ScopedCookie> entries;

  public Scoped get(Object key) throws NullPointerException {
    String name = nameOf(key);
    if (name != null) {
      if (entries != null) {
        ScopedCookie lifeCycle = entries.get(name);
        if (lifeCycle != null) {
          return lifeCycle.scoped;
        }
      }
    }
    return null;
  }

  public void set(Object key, Scoped scoped) throws NullPointerException {
    String name = nameOf(key);
    if (name == null) {
      throw new UndeclaredIOException(new NotSerializableException("Type should be named with @" + Named.class.getSimpleName()));
    }
    if (!(scoped.get() instanceof Serializable)) {
      throw new UndeclaredIOException(new NotSerializableException("Could not serialize object"));
    }
    if (entries == null) {
      entries = new HashMap<String, ScopedCookie>();
    }
    entries.put(name, new ScopedCookie(TO_SEND, scoped));
  }

  private String nameOf(Object key) {
    Key gkey = (Key)key;
    Class<?> type = gkey.getTypeLiteral().getRawType();
    Named name = type.getAnnotation(Named.class);
    return name != null ? name.value() : null;
  }

  public int size() {
    return entries != null ? entries.size() : 0;
  }

  public void close() {
    if (entries != null) {
      for (Map.Entry<String, ScopedCookie> e : entries.entrySet()) {
        ScopedCookie lifeCycle = e.getValue();
        if (lifeCycle.status == RECEIVED) {
          e.setValue(new ScopedCookie(TO_REMOVE, lifeCycle.scoped));
        }
      }
    }
  }

  public Iterator<Scoped> iterator() {
    return Tools.emptyIterator();
  }
}
