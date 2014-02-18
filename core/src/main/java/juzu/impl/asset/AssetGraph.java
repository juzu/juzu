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
package juzu.impl.asset;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Julien Viet
 */
class AssetGraph extends HashMap<String, Set<String>> {

  AssetGraph() {
  }

  AssetGraph(AssetGraph that) {
    super(that);
  }

  boolean register(String id, String deployedId) {
    if (check(deployedId, id)) {
      Set<String> a = get(id);
      if (a == null) {
        put(id, Collections.singleton(deployedId));
      } else if (!(a instanceof HashSet<?>)) {
        a = new HashSet<String>(a);
        a.add(deployedId);
        put(id, Collections.unmodifiableSet(a));
      }
      return true;
    } else {
      return false;
    }
  }

  void unregister(String id, String deployedId) {
    Set<String> a = new HashSet<String>(get(id));
    a.remove(deployedId);
    put(id, Collections.unmodifiableSet(a));
  }

  private boolean check(String node, String id) {
    Set<String> s = get(node);
    if (s != null && s.size() > 0) {
      if (s.contains(id)) {
        return false;
      } else {
        for (String t : s) {
          if (!check(t, id)) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
