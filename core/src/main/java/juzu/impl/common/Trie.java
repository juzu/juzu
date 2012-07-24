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
public class Trie<K, V> implements Iterable<K> {

  /** . */
  private final Trie<K, V> parent;

  /** . */
  private final List<K> path;

  /** . */
  private final K key;

  /** The entries. */
  private Map<K, Trie<K, V>> entries;

  /** . */
  private V value;

  public Trie() {
    this.parent = null;
    this.path = Collections.emptyList();
    this.key = null;
  }

  private Trie(Trie<K, V> parent, K key) {
    ArrayList<K> path = new ArrayList<K>(parent.path.size() + 1);
    path.addAll(parent.path);
    path.add(key);

    //
    this.parent = parent;
    this.path = path;
    this.key = key;
  }

  // Deep copy constructor
  private Trie(Trie<K, V> parent, Trie<K, V> that) {
    HashMap<K, Trie<K, V>> entries;
    if (that.entries != null) {
      entries = new HashMap<K, Trie<K, V>>(that.entries);
      for (Map.Entry<K, Trie<K, V>> entry : entries.entrySet()) {
        entry.setValue(new Trie<K, V>(this, entry.getValue()));
      }
    }
    else {
      entries = null;
    }

    //
    this.parent = parent;
    this.path = that.path;
    this.key = that.key;
    this.entries = entries;
    this.value = that.value;
  }

  public Trie<K, V> getParent() {
    return parent;
  }

  public K getKey() {
    return key;
  }

  public V value() {
    return value;
  }

  public V value(V value) {
    try {
      return this.value;
    }
    finally {
      this.value = value;
    }
  }

  public Iterator<K> iterator() {
    return entries != null ? entries.keySet().iterator() : Tools.<K>emptyIterator();
  }

  public Iterable<K> getPath() {
    return path;
  }

  public Iterator<Trie<K, V>> getEntries() {
    return entries.values().iterator();
  }

  public Trie<K, V> get(K... keys) {
    return get(keys, 0, keys.length);
  }

  public Trie<K, V> get(K key) {
    if (entries != null) {
      return entries.get(key);
    }
    return null;
  }

  public Trie<K, V> get(K[] keys, int from, int to) {
    Trie<K, V> ret;
    if (from == to) {
      ret = this;
    }
    else {
      K key = keys[from];
      Trie<K, V> entry = get(key);
      ret = entry != null ? entry.get(keys, from + 1, to) : null;
    }
    return ret;
  }

  public Trie<K, V> get(Iterator<K> keys) {
    Trie<K, V> ret;
    if (keys.hasNext()) {
      K key = keys.next();
      Trie<K, V> entry = get(key);
      ret = entry != null ? entry.get(keys) : null;
    }
    else {
      ret = this;
    }
    return ret;
  }

  public Trie<K, V> add(K key) {
    Trie<K, V> entry;
    if (entries == null) {
      entries = new HashMap<K, Trie<K, V>>();
      entry = null;
    }
    else {
      entry = entries.get(key);
    }
    if (entry == null) {
      entries.put(key, entry = new Trie<K, V>(this, key));
    }
    return entry;
  }

  public Trie<K, V> add(K... keys) {
    return add(keys, 0, keys.length);
  }

  public Trie<K, V> add(K[] keys, int from, int to) {
    if (from == to) {
      return this;
    }
    else {
      K key = keys[from];
      Trie<K, V> entry = add(key);
      return entry.add(keys, from + 1, to);
    }
  }

  public Trie<K, V> add(Iterator<K> keys) {
    if (keys.hasNext()) {
      K key = keys.next();
      Trie<K, V> entry = add(key);
      return entry.add(keys);
    }
    else {
      return this;
    }
  }

  public void merge(Trie<K, V> merged) {
    if (path.size() > 0) {
      throw new IllegalStateException("Cannot merge at non root trie");
    }
    if (merged.path.size() > 0) {
      throw new IllegalArgumentException("Cannot merge non root trie");
    }
    doMerge(merged);
  }

  private void doMerge(Trie<K, V> merged) {
    if (merged.entries != null) {
      for (Trie<K, V> mergedEntry : merged.entries.values()) {
        K key = mergedEntry.key;
        Trie<K, V> entry = get(key);
        if (entry == null) {
          if (entries == null) {
            entries = new HashMap<K, Trie<K, V>>();
          }
          entries.put(key, new Trie<K, V>(this, mergedEntry));
        }
        else {
          entry.doMerge(mergedEntry);
        }
      }
    }
    else {
      value = merged.value;
    }
  }
}
