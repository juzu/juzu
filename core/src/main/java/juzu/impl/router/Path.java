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

package juzu.impl.router;

import juzu.UndeclaredIOException;
import juzu.impl.common.PercentCodec;

import java.io.ByteArrayOutputStream;
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

  /** Constant. */
  public static final Path SLASH = Path.parse("/");

  private static final class Data {

    private int hex(char c) {
      if (c >= '0' && c <= '9') {
        return c - '0';
      }
      else if (c >= 'A' && c <= 'F') {
        return c + 10 - 'A';
      }
      else if (c >= 'a' && c <= 'f') {
        return c + 10 - 'a';
      }
      else {
        throw new IllegalArgumentException("Invalid hex code in " + rawValue);
      }
    }

    /** . */
    private final String rawValue;

    /** . */
    private final String value;

    /** . */
    private final int[] mapping;

    private Data(String rawValue) throws IOException, IllegalArgumentException {
      this.rawValue = rawValue;

      //
      int len = rawValue.length();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int[] mapping = new int[len];
      int count = 0;

      //
      int i = 0;
      while (i < len) {
        char c = rawValue.charAt(i);
        mapping[count++] = i;

        //
        if (PercentCodec.PATH_SEGMENT.accept(c)) {
          baos.write((int)c);
          i++;
        }
        else if (c == '%') {
          if (i + 2 >= len) {
            throw new IllegalArgumentException("Invalid percent escape in " + rawValue);
          }
          int h = (hex(rawValue.charAt(i + 1)) << 4) + hex(rawValue.charAt(i + 2));
          baos.write(h);
          i += 3;

          // Compute the number of bytes to read for this char
          int size = 0;
          for (int j = h;(j & 0x80) != 0;j = j << 1) {
            size++;
          }
          if (size == 0) {
            size = 1;
          }
          else if (size > 6) {
            throw new IllegalArgumentException("Invalid percent escape in " + rawValue);
          }

          // Compute the offset we need to read those bytes
          int to = i + (size - 1) * 3;
          if (to > len) {
            throw new IllegalArgumentException("Invalid percent escape in " + rawValue);
          }

          // Read what we need
          while (i < to) {
            if (rawValue.charAt(i) != '%') {
              throw new IllegalArgumentException("Invalid percent escape in " + rawValue);
            }
            h = (hex(rawValue.charAt(i + 1)) << 4) + hex(rawValue.charAt(i + 2));
            baos.write(h);
            i += 3;
          }
        }
        else if (c == '/') {
          baos.write('/');
          i++;
        }
        else {
          throw new IllegalArgumentException("Unsupported char value in path " + (int)c + " / " + c);
        }
      }

      //
      this.value = baos.toString("UTF-8");
      this.mapping = mapping;
    }

    int getRawStart(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("No negative index accepted");
      }
      if (index >= value.length()) {
        throw new IndexOutOfBoundsException("Index can't be greater than length");
      }
      return mapping[index];
    }

    int getRawEnd(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("No negative index accepted");
      }
      if (index >= value.length()) {
        throw new IndexOutOfBoundsException("Index can't be greater than length");
      }
      index++;
      if (index == value.length()) {
        return rawValue.length();
      }
      else {
        return mapping[index];
      }
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

  int getRawStart(int index) {
    return data.getRawStart(innerIndex(index)) - data.getRawStart(offset);
  }

  int getRawEnd(int index) {
    return data.getRawEnd(innerIndex(index)) - data.getRawStart(offset);
  }

  int getRawLength(int index) {
    return getRawEnd(index) - getRawStart(index);
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
      if (i >= data.value.length()) {
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
}
