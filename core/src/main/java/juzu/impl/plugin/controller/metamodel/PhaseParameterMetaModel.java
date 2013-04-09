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

package juzu.impl.plugin.controller.metamodel;

import juzu.impl.common.Cardinality;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PhaseParameterMetaModel extends ParameterMetaModel {

  /** . */
  final Cardinality cardinality;

  /** . */
  final ElementHandle.Class type;

  public PhaseParameterMetaModel(
      String name,
      Cardinality cardinality,
      ElementHandle.Class type,
      String typeLiteral) {
    super(name, typeLiteral);

    //
    this.cardinality = cardinality;
    this.type = type;
  }

  public Cardinality getCardinality() {
    return cardinality;
  }

  public ElementHandle.Class getType() {
    return type;
  }

  @Override
  public JSON toJSON() {
    return new JSON().
        set("name", name).
        set("type", type).
        set("typeLiteral", typeLiteral).
        set("cardinality", cardinality);
  }
}
