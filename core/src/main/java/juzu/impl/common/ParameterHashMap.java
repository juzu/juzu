/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (Iterator<Map.Entry<String, String[]>> i = entrySet().iterator();i.hasNext();) {
      Map.Entry<String, String[]> entry = i.next();
      sb.append(entry.getKey()).append("=[");
      String[] value = entry.getValue();
      for (int j = 0;j < value.length;j++) {
        if (j > 0) {
          sb.append(',');
        }
        sb.append(value[j]);
      }
      sb.append(']');
      if (i.hasNext()) {
        sb.append(',');
      }
    }
    sb.append('}');
    return sb.toString();
  }
}
