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
import juzu.impl.common.Tools;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PhaseParameter<V> extends ControlParameter {

  /** . */
  private final Cardinality cardinality;

  /** . */
  private final String alias;

  /** . */
  private final Class<V> valueType;

  public PhaseParameter(
      String name,
      Class<?> type,
      Class<V> valueType,
      Cardinality cardinality,
      String alias) throws NullPointerException {
    super(name, type);

    //
    if (cardinality == null) {
      throw new NullPointerException("No null parameter cardinality accepted");
    }

    //
    this.cardinality = cardinality;
    this.alias = alias;
    this.valueType = valueType;
  }

  public PhaseParameter(
      String name,
      AnnotatedElement annotations,
      Class<?> type,
      Class<V> valueType,
      Cardinality cardinality,
      String alias) throws NullPointerException {
    super(name, annotations, type);

    //
    if (cardinality == null) {
      throw new NullPointerException("No null parameter cardinality accepted");
    }

    //
    this.cardinality = cardinality;
    this.alias = alias;
    this.valueType = valueType;
  }

  /**
   * Return the parameter mapped name.
   *
   * @return the mapped name
   */
  public String getMappedName() {
    return alias != null ? alias : name;
  }

  public String getAlias() {
    return alias;
  }

  public Class<V> getValueType() {
    return valueType;
  }

  public Object getValue(List<V> values) {
    switch (cardinality) {
      case SINGLE:
        return (values.size() > 0) ? values.get(0) : null;
      case ARRAY:
        Object array = Array.newInstance(getValueType(), values.size());
        for (int i = 0;i < values.size();i++) {
          Array.set(array, i, values.get(i));
        }
        return array;
      case LIST:
        return values;
      default:
        throw new UnsupportedOperationException("Handle me gracefully");
    }
  }

  /**
   * Returns the parameter cardinality.
   *
   * @return the parameter cardinality
   */
  public Cardinality getCardinality() {
    return cardinality;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PhaseParameter) {
      PhaseParameter that = (PhaseParameter)obj;
      return super.equals(that) && cardinality.equals(that.cardinality) && Tools.safeEquals(alias, that.alias);
    } else {
      return false;
    }
  }
}
