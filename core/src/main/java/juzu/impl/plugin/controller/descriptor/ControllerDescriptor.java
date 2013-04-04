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

package juzu.impl.plugin.controller.descriptor;

import juzu.impl.request.Method;

import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerDescriptor {

  /** . */
  private final Class<?> type;

  /** . */
  private final List<Method<?>> methods;

  public ControllerDescriptor(Class<?> type, List<Method<?>> methods) {
    this.type = type;
    this.methods = Collections.unmodifiableList(methods);
  }

  public String getTypeName() {
    return type.getName();
  }

  public Class<?> getType() {
    return type;
  }

  public List<Method<?>> getMethods() {
    return methods;
  }
}
