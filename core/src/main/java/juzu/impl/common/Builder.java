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
