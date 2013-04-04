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

package juzu.impl.common;

import java.util.ArrayList;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Builder {

  public static <K, V> Map<K, V> map(K key, V value) {
    return new Map<K, V>().map(key, value);
  }

  public static class Map<K, V> {

    /** . */
    private HashMap<K, V> map;

    private Map() {
      this.map = new HashMap<K, V>();
    }

    public Map<K, V> map(K key, V value) {
      map.put(key, value);
      return this;
    }

    public java.util.Map<K, V> build() {
      return map;
    }
  }

  public static <E> List<E> list(E elt) {
    return new List<E>().add(elt);
  }

  public static class List<E> {

    /** . */
    private ArrayList<E> list;

    private List() {
      this.list = new ArrayList<E>();
    }

    public List<E> add(E elt) {
      list.add(elt);
      return this;
    }

    public java.util.List<E> build() {
      return list;
    }
  }
}
