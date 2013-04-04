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

import javax.inject.Named;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NameLiteral extends AnnotationLiteral<Named> implements Named {

  /** . */
  private final String value;

  /**
   * Create a new name literal implementing the {@link Named} annotation interface.
   *
   * @param value the name value
   * @throws NullPointerException if the value is null
   */
  public NameLiteral(String value) throws NullPointerException {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }
}
