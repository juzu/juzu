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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ParameterHashMap extends HashMap<String, String[]> implements ParameterMap {

  public void setParameter(String name, String value) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null name can be used");
    }
    if (name.startsWith("juzu.")) {
      throw new IllegalArgumentException("Parameter name cannot be prefixed with juzu.");
    }
    if (value != null) {
      put(name, new String[]{value});
    }
    else {
      remove(name);
    }
  }

  public void setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException("No null name can be used");
    }
    if (value == null) {
      throw new NullPointerException("No null value can be used");
    }
    if (name.startsWith("juzu.")) {
      throw new IllegalArgumentException("Parameter name cannot be prefixed with juzu.");
    }
    if (value.length == 0) {
      remove(name);
    }
    else {
      for (String component : value) {
        if (component == null) {
          throw new IllegalArgumentException("Argument array cannot contain null value");
        }
      }
      put(name, value.clone());
    }
  }

  public void setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
    if (parameters == null) {
      throw new NullPointerException("No null parameters accepted");
    }
    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      if (entry.getKey() == null) {
        throw new IllegalArgumentException("No null parameter key can be null");
      }
      if (entry.getValue() == null) {
        throw new IllegalArgumentException("No null parameter value can be null");
      }
      setParameter(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ParameterMap) {
      ParameterMap that = (ParameterMap)o;
      if (keySet().equals(that.keySet())) {
        for (Map.Entry<String, String[]> entry : entrySet()) {
          String[] value1 = entry.getValue();
          String[] value2 = that.get(entry.getKey());
          if (value2 == null || !Arrays.equals(value1, value2)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return Tools.toString(this.entrySet(), new StringBuilder()).toString();
  }
}
