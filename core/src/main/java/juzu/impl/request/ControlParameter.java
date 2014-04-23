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

import juzu.impl.common.AbstractAnnotatedElement;

import java.lang.reflect.AnnotatedElement;

/**
 * A parameter of a controller.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ControlParameter {

  /** . */
  protected final String name;

  /** . */
  private final Class<?> type;

  /** . */
  private final AnnotatedElement annotations;

  public ControlParameter(String name, Class<?> type) throws NullPointerException {
    this(name, AbstractAnnotatedElement.EMPTY, type);
  }

  public ControlParameter(String name, AnnotatedElement annotations, Class<?> type) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null parameter name accepted");
    }

    //
    this.name = name;
    this.type = type;
    this.annotations = annotations;
  }

  /**
   * @return the parameter name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the parameter class type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * @return the parameter annotations
   */
  public AnnotatedElement getAnnotations() {
    return annotations;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    else if (obj instanceof ControlParameter) {
      ControlParameter that = (ControlParameter)obj;
      return name.equals(that.name);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[name=" + name + "]";
  }
}
