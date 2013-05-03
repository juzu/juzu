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

package juzu.impl.request;

import java.lang.reflect.Type;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ContextualParameter extends ControlParameter {

  /** . */
  private final Type genericType;

  public ContextualParameter(String name, Class<?> classType) throws NullPointerException {
    this(name, classType, classType);
  }

  public ContextualParameter(String name, Class<?> classType, Type genericType) throws NullPointerException {
    super(name, classType);

    //
    this.genericType = genericType;
  }

  public Type getGenericType() {
    return genericType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ContextualParameter) {
      ContextualParameter that = (ContextualParameter)obj;
      return super.equals(that) && genericType.equals(that.genericType);
    } else {
      return false;
    }
  }
}
