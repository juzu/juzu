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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PhaseParameterMetaModel extends ParameterMetaModel {

  /** . */
  final Cardinality cardinality;

  /** . */
  final String valueType;

  /** . */
  final String alias;

  public PhaseParameterMetaModel(
      String name,
      Cardinality cardinality,
      String typeLiteral,
      String valueType,
      String alias) {
    super(name, typeLiteral);

    //
    this.cardinality = cardinality;
    this.alias = alias;
    this.valueType = valueType;
  }

  public Cardinality getCardinality() {
    return cardinality;
  }

  public String getValueType() {
    return valueType;
  }

  public String getAlias() {
    return alias;
  }

  public String getMappedName() {
    return alias != null ? alias : name;
  }

  @Override
  public JSON toJSON() {
    return new JSON().
        set("name", name).
        set("valueType", valueType).
        set("type", type).
        set("cardinality", cardinality);
  }
}
