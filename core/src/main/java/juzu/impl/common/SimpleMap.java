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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Make implementation of maps easier. Note that this map is not optimized for speed on all operations, the goal
 * of this map is to make easy the implementation of maps.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class SimpleMap<K, V> extends AbstractMap<K, V> {

  protected abstract Iterator<K> keys();

  @Override
  public abstract V get(Object key);

  @Override
  public final boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public final Set<Entry<K, V>> entrySet() {
    return entries;
  }

  private AbstractSet<Entry<K, V>> entries = new AbstractSet<Entry<K, V>>() {

    @Override
    public Iterator<Entry<K, V>> iterator() {
      final Iterator<K> names = keys();
      return new Iterator<Entry<K, V>>() {
        public boolean hasNext() {
          return names.hasNext();
        }

        public Entry<K, V> next() {
          final K name = names.next();
          return new Entry<K, V>() {
            public K getKey() {
              return name;
            }

            public V getValue() {
              return get(name);
            }

            public V setValue(V value) {
              throw new UnsupportedOperationException();
            }
          };
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public int size() {
      int size = 0;
      for (Iterator<K> names = keys();names.hasNext();) {
        size++;
      }
      return size;
    }
  };
}
