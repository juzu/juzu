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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopedKey {

  /** . */
  private final Scope scope;

  /** . */
  private final Object scoped;

  ScopedKey(Scope scope, Object scoped) {
    this.scope = scope;
    this.scoped = scoped;
  }

  public Scope getScope() {
    return scope;
  }

  public Object getScoped() {
    return scoped;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ScopedKey) {
      ScopedKey that = (ScopedKey)obj;
      return scope == that.scope && scoped.equals(that.scoped);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return scope.hashCode() ^ scoped.hashCode();
  }
}
