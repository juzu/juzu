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

package juzu.impl.bridge;

import juzu.io.Encoding;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Parameter {

  public static Parameter create(String name, String value) {
    return create(Encoding.RFC3986, name, value);
  }

  public static Parameter create(String name, String... value) {
    return create(Encoding.RFC3986, name, value);
  }

  public static Parameter create(Encoding encoded, String name, String value) {
    return new SingleValued(encoded, name, value);
  }

  public static Parameter create(Encoding encoded, String name, String... value) {
    return new MultiValued(encoded, name, value);
  }

  /** . */
  final Encoding encoding;

  /** . */
  final String name;

  private Parameter(Encoding encoding, String name) {
    this.encoding = encoding;
    this.name = name;
  }

  public Encoding getEncoding() {
    return encoding;
  }

  public String getName() {
    return name;
  }

  public String getValue(int index) {
    throw new UnsupportedOperationException();
  }

  public int getSize() {
    throw new UnsupportedOperationException();
  }

  public String[] toArray() {
    throw new UnsupportedOperationException();
  }

  private static class SingleValued extends Parameter {

    /** . */
    private final String value;

    private SingleValued(Encoding encoded, String name, String value) {
      super(encoded, name);

      /** . */
      this.value = value;
    }

    @Override
    public String getValue(int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException("Wrong index " + index);
      }
      return value;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public String[] toArray() {
      return new String[]{value};
    }

    @Override
    public String toString() {
      return "Parameter[encoded=" + encoding + ",name=" + name + ",value=" + value + "]";
    }
  }

  private static class MultiValued extends Parameter {

    /** . */
    private final String[] value;

    private MultiValued(Encoding encoding, String name, String[] value) {
      super(encoding, name);

      /** . */
      this.value = value;
    }

    @Override
    public String getValue(int index) {
      if (index < 0 || index > value.length) {
        throw new IndexOutOfBoundsException("Wrong index " + index);
      }
      return value[index];
    }

    @Override
    public int getSize() {
      return value.length;
    }

    @Override
    public String[] toArray() {
      return value.clone();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("Parameter[encoding=").append(encoding.getDisplayName()).append(",name=").append(name).append(",value=(");
      boolean first = true;
      for (String s : value) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }
        sb.append(s);
      }
      sb.append(")]");
      return sb.toString();
    }
  }
}
