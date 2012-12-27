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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTrie<K, T extends AbstractTrie<K, T>> implements Iterable<K> {

  /** . */
  final T parent;

  /** . */
  final List<K> path;

  /** . */
  final K key;

  /** The entries. */
  private Map<K, T> entries;

  protected AbstractTrie() {
    this.parent = null;
    this.path = Collections.emptyList();
    this.key = null;
  }

  protected AbstractTrie(T parent, K key) {
    ArrayList<K> path = new ArrayList<K>(parent.path.size() + 1);
    path.addAll(parent.path);
    path.add(key);

    //
    this.parent = parent;
    this.path = Collections.unmodifiableList(path);
    this.key = key;
  }

  protected abstract T create(T parent, K key);

  public final T getParent() {
    return parent;
  }

  public final K getKey() {
    return key;
  }

  public final Iterator<K> iterator() {
    return entries != null ? entries.keySet().iterator() : Tools.<K>emptyIterator();
  }

  public final Iterable<K> getPath() {
    return path;
  }

  public final Iterator<T> getEntries() {
    return entries.values().iterator();
  }

  public final T get(K... keys) {
    return get(keys, 0, keys.length);
  }

  public final T get(K key) {
    if (entries != null) {
      return entries.get(key);
    }
    return null;
  }

  public final T get(K[] keys, int from, int to) {
    T ret;
    if (from == to) {
      ret = (T)this;
    }
    else {
      K key = keys[from];
      T entry = get(key);
      ret = entry != null ? entry.get(keys, from + 1, to) : null;
    }
    return ret;
  }

  public final T get(Iterator<K> keys) {
    T ret;
    if (keys.hasNext()) {
      K key = keys.next();
      T entry = get(key);
      ret = entry != null ? entry.get(keys) : null;
    }
    else {
      ret = (T)this;
    }
    return ret;
  }

  public final T add(K key) {
    T entry;
    if (entries == null) {
      entries = new HashMap<K, T>();
      entry = null;
    }
    else {
      entry = entries.get(key);
    }
    if (entry == null) {
      entries.put(key, entry = create((T)this, key));
    }
    return entry;
  }

  public final T add(K... keys) {
    return add(keys, 0, keys.length);
  }

  public final T add(K[] keys, int from, int to) {
    if (from == to) {
      return (T)this;
    }
    else {
      K key = keys[from];
      T entry = add(key);
      return entry.add(keys, from + 1, to);
    }
  }

  public final T add(Iterator<K> keys) {
    if (keys.hasNext()) {
      K key = keys.next();
      T entry = add(key);
      return entry.add(keys);
    }
    else {
      return (T)this;
    }
  }
}
