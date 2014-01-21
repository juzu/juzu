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
    return entries != null ? entries.values().iterator() : Tools.<T>emptyIterator();
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
