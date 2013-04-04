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
public class Trie<K, V> extends AbstractTrie<K, Trie<K, V>> {

  /** . */
  private V value;

  public Trie() {
  }

  private Trie(Trie<K, V> parent, K key) {
    super(parent, key);
  }

  @Override
  protected Trie<K, V> create(Trie<K, V> parent, K key) {
    return new Trie<K, V>(parent, key);
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
}
