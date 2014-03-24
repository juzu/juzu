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

package juzu.request;

import juzu.impl.common.Tools;

import java.util.Arrays;
import java.util.Map;

/**
 * A unmodifiable request parameter, it provides access to the parameter name and the parameter values.
 * The parameter name and values are decoded according, the request parameter provides access to the
 * raw parameter x-www-formurl-encoded values when they are available.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class RequestParameter extends Parameter {

  /**
   * Create new parameter.
   *
   * @param name the parameter name
   * @param value the parameter value
   * @return the new parameter
   * @throws NullPointerException if the name of the value is null
   */
  public static RequestParameter create(String name, String value) throws NullPointerException {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    return new RequestParameter(name, null, new String[]{value});
  }

  /**
   * Create new parameter.
   *
   * @param name the parameter name
   * @param value the parameter value
   * @return the new parameter
   * @throws NullPointerException if the name of the value is null
   */
  public static RequestParameter create(String name, String raw, String value) throws NullPointerException {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    return new RequestParameter(name, new String[]{raw}, new String[]{value});
  }

  /**
   * Create new parameter.
   *
   * @param name the parameter name
   * @param value the parameter value
   * @return the new parameter
   * @throws NullPointerException if the name of the value is null
   * @throws IllegalArgumentException if the value is empty or contains a null component
   */
  public static RequestParameter create(String name, String[] value) throws NullPointerException, IllegalArgumentException {
    return new RequestParameter(name, null, value);
  }

  /**
   * Create new parameter.
   *
   * @param entry the parameter entry
   * @return the new parameter
   * @throws NullPointerException if the entry is null
   * @throws IllegalArgumentException if entry provides a null name or an illegal value
   */
  public static RequestParameter create(Map.Entry<String, String[]> entry) throws NullPointerException, IllegalArgumentException {
    if (entry == null) {
      throw new NullPointerException("No null entry accepted");
    }
    String name = entry.getKey();
    if (name == null) {
      throw new IllegalArgumentException("No null name accepted");
    }
    String[] value = entry.getValue();
    if (value == null) {
      throw new IllegalArgumentException("No null value accepted");
    }
    return new RequestParameter(name, null, value);
  }

  /** . */
  private final String name;

  /** . */
  private final String[] raw;

  /** . */
  private final String[] value;

  private RequestParameter(String name, String[] raw, String[] value) throws NullPointerException, IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    for (String s : value) {
      if (s == null) {
        throw new IllegalArgumentException("Parameter value cannot contain null");
      }
    }
    if (value.length == 0) {
      throw new IllegalArgumentException("Value length cannot be lesser than 1");
    }

    //
    this.name = name;
    this.raw = raw;
    this.value = value;
  }

  @Override
  public String get(int index) {
    if (index < 0 || index > value.length) {
      throw new IndexOutOfBoundsException("Bad index " + index);
    }
    return value[index];
  }

  @Override
  public int size() {
    return value.length;
  }

  /**
   * @return the parameter name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the first parameter value
   */
  public String getValue() {
    return value[0];
  }

  /**
   * Returns the value at specified index.
   *
   * @param index the index of the value
   * @return the value or null if the index is out of bounds
   */
  public String getRaw(int index) {
    if (index < 0 || index > value.length) {
      throw new IndexOutOfBoundsException("Bad index " + index);
    } else {
      return raw != null ? raw[index] : null;
    }
  }

  /**
   * Clone the value and returns it.
   *
   * @return the value as a <code>String[]</code>
   */
  public String[] toArray() {
    return value.clone();
  }

  /**
   * Add this parameter to a map.
   *
   * @param map the map to add to
   * @param <M> the map generic type
   * @return the map argument
   */
  public <M extends Map<String, RequestParameter>> M appendTo(M map) {
    RequestParameter param = map.get(name);
    if (param != null) {
      map.put(name, param.append(this));
    } else {
      map.put(name, this);
    }
    return map;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof RequestParameter) {
      RequestParameter that = (RequestParameter)obj;
      return name.equals(that.name) && Arrays.equals(value, that.value);
    } else {
      return false;
    }
  }

  public RequestParameter append(RequestParameter appended) {
    return new RequestParameter(name, Tools.safeConcat(raw, appended.raw), Tools.safeConcat(value, appended.value));
  }

  public RequestParameter append(String[] appended) {
    String[] value = new String[this.value.length + 1];
    System.arraycopy(this.value, 0, value, 0, this.value.length);
    System.arraycopy(appended, 0, value, appended.length, this.value.length);
    return new RequestParameter(name, null, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RequestParameter[name=").append(name).append(",value=(");
    for (int i = 0;i < value.length;i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(value[i]);
    }
    sb.append(")]");
    return sb.toString();
  }
}
