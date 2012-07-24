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

package juzu.test;

import juzu.impl.common.Tools;

import java.util.HashMap;
import java.util.Map;

/**
 * A static map used in various manner by tests.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Registry {

  private static final Map<Object, Object> state = new HashMap<Object, Object>();

  public static <T> T get(Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    // Absolutely not type safe, but we don't care, it's for testing
    @SuppressWarnings("unchecked")
    T t = (T)state.get(key);
    return t;
  }

  public static <T> T compareAndSet(Object key, T expectedValue, T value) {
    if (key == null) {
      throw new NullPointerException();
    }
    T previous = (T)state.get(key);
    if (Tools.safeEquals(previous, expectedValue)) {
      state.put(key, value);
    }
    return previous;
  }

  public static <T> void set(Object key, T value) {
    if (key == null) {
      throw new NullPointerException();
    }
    if (value != null) {
      state.put(key, value);
    }
    else {
      state.remove(key);
    }
  }

  public static <T> T unset(Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return (T)state.remove(key);
  }

  public static void clear() {
    state.clear();
  }
}
