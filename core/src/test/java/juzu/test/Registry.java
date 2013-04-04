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
