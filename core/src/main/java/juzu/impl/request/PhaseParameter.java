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

import juzu.impl.common.Cardinality;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PhaseParameter extends Parameter {

  /** . */
  private final Cardinality cardinality;

  public PhaseParameter(String name, Class<?> type, Cardinality cardinality) throws NullPointerException {
    super(name, type);

    //
    if (cardinality == null) {
      throw new NullPointerException("No null parameter cardinality accepted");
    }

    //
    this.cardinality = cardinality;
  }

  @Override
  public PhaseArgument create(Object value) {
    return new PhaseArgument(this, value);
  }

  /**
   * Returns the parameter cardinality.
   *
   * @return the parameter cardinality
   */
  public Cardinality getCardinality() {
    return cardinality;
  }
}
