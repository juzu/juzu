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

package juzu.impl.router;

import juzu.UndeclaredIOException;
import juzu.impl.common.PercentCodec;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Path {

  static Path parse(String path) throws UndeclaredIOException {
    try {
      Data data = new Data(path);
      return new Path(data, 0);
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  private static final class Data {

    /** . */
    private final String value;

    /** . */
    private final boolean[] escaped;

    private Data(String rawValue) throws IOException, IllegalArgumentException {
      int len = rawValue.length();
      StringBuilder buffer = new StringBuilder();
      boolean[] escaped = new boolean[len];
      int count = 0;

      //
      int i = 0;
      while (i < len) {
        int delta = PercentCodec.PATH.decodeChar(rawValue, i, len, buffer);
        escaped[count++] = delta > 1;
        i += delta;
      }

      //
      this.value = buffer.toString();
      this.escaped = escaped;
    }

    boolean isEscaped(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("No negative index accepted");
      }
      if (index >= value.length()) {
        throw new IndexOutOfBoundsException("Index can't be greater than length");
      }
      return escaped[index];
    }
  }

  /** . */
  private final Data data;

  /** . */
  private final int offset;

  /** . */
  private final String value;

  private Path(Data data, int offset) {
    this.data = data;
    this.offset = offset;
    this.value = data.value.substring(offset);
  }

  String getValue() {
    return value;
  }

  boolean isEscaped(int index) {
    return data.isEscaped(innerIndex(index));
  }

  char charAt(int pos) {
    return value.charAt(pos);
  }

  int length() {
    return value.length();
  }

  int indexOf(int c, int index) {
    return value.indexOf(c, index);
  }

  Path subPath(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("No negative index accepted");
    }
    else if (index == 0) {
      return this;
    }
    else {
      int i = offset + index;
      if (i > data.value.length()) {
        throw new IndexOutOfBoundsException("Index can't be greater than length");
      }
      return new Path(data, i);
    }
  }

  /**
   * Convert the specified index to the internal index.
   *
   * @param outterIndex the outter index
   * @return the inner index value
   * @throws IndexOutOfBoundsException if the outter index is not correct
   */
  private int innerIndex(int outterIndex) throws IndexOutOfBoundsException {
    if (outterIndex < 0) {
      throw new IndexOutOfBoundsException("No negative index accepted");
    }
    int pos = offset + outterIndex;
    if (pos > data.value.length()) {
      throw new IndexOutOfBoundsException("Index can't be greater than length");
    }
    return pos;
  }

  @Override
  public String toString() {
    return "Path[" + value + "]";
  }
}
