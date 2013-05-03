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
 * A request parameter.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class RequestParameter {

  public static RequestParameter create(String name, String value) {
    return new RequestParameter(name, null, new String[]{value});
  }

  public static RequestParameter create(String name, String raw, String value) {
    return new RequestParameter(name, new String[]{raw}, new String[]{value});
  }

  public static RequestParameter create(String name, String[] value) {
    return new RequestParameter(name, null, value);
  }

  public static RequestParameter create(Map.Entry<String, String[]> entry) {
    return new RequestParameter(entry.getKey(), null, entry.getValue());
  }

  /** . */
  private final String name;

  /** . */
  private final String[] raw;

  /** . */
  private final String[] value;

  private RequestParameter(String name, String[] raw, String[] value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return value.length;
  }

  public String getValue() {
    return value.length > 0 ? value[0] : null;
  }

  public String getValue(int index) {
    if (index < 0 || index > value.length) {
      throw new IndexOutOfBoundsException("Wrong index: " + index);
    }
    return value[index];
  }

  public String getRaw(int index) {
    if (index < 0 || index > value.length) {
      throw new IndexOutOfBoundsException("Wrong index: " + index);
    }
    return raw != null ? raw[index] : null;
  }

  public String[] toArray() {
    return value.clone();
  }

  public <M extends Map<String, RequestParameter>> M addTo(M map) {
    map.put(name, this);
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
