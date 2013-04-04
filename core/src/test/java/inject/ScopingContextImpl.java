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

package inject;

import juzu.Scope;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopingContextImpl implements ScopingContext {

  /** . */
  private final Map<ScopedKey, Scoped> entries = new HashMap<ScopedKey, Scoped>();

  public Scoped getContextualValue(Scope scope, Object key) {
    return entries.get(new ScopedKey(scope, key));
  }

  public void setContextualValue(Scope scope, Object key, Scoped value) {
    if (value != null) {
      entries.put(new ScopedKey(scope, key), value);
    }
    else {
      entries.remove(new ScopedKey(scope, key));
    }
  }

  public boolean isActive(Scope scope) {
    return true;
  }

  public Map<ScopedKey, Scoped> getEntries() {
    return entries;
  }
}
