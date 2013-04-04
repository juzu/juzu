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

package juzu.impl.inject.spi.cdi;

import juzu.impl.inject.Scoped;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CDIScoped<T> implements Scoped {

  /** . */
  final Contextual<T> contextual;

  /** . */
  final CreationalContext<T> creationalContext;

  /** . */
  final T object;

  CDIScoped(Contextual<T> contextual, CreationalContext<T> creationalContext, T object) {
    this.contextual = contextual;
    this.creationalContext = creationalContext;
    this.object = object;
  }

  public Object get() {
    return object;
  }

  public void destroy() {
    contextual.destroy(object, creationalContext);
  }
}
