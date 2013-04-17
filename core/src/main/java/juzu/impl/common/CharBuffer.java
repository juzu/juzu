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

import java.io.IOException;

/**
 * A growing char buffer that can be read and written.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CharBuffer implements Appendable {

  /** The initial size. */
  private static final int INITIAL_SIZE = 16;

  /** . */
  private char[] chars;

  /** . */
  private int from;

  /** . */
  private int to;

  public CharBuffer() {
    this(INITIAL_SIZE);
  }

  public CharBuffer(int length) {
    if (length < 1) {
      throw new IllegalArgumentException();
    }
    this.chars = new char[length];
    this.from = 0;
    this.to = 0;
  }

  public int getLength() {
    return to - from;
  }

  public int getSize() {
    return chars.length;
  }

  /**
   * Read from a char array.
   *
   * @param src the char array source
   * @param at the first index of the chars to read
   * @param len the amount of chars to read
   * @throws NullPointerException if the source is null
   * @throws IllegalArgumentException if the len argument is negative
   * @throws IndexOutOfBoundsException if the source is adressed out of its bounds
   */
  public void readFrom(char[] src, int at, int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
    if (src == null) {
      throw new NullPointerException("Buffer cannot be null");
    }
    if (at < 0) {
      throw new IndexOutOfBoundsException("Begin pointer cannot be negative: " + at);
    }
    if (len < 0) {
      throw new IllegalArgumentException("Length cannot be negative: " + len);
    }
    if (at + len > src.length) {
      throw new IndexOutOfBoundsException("End pointer cannot be greater than buffer: " + (at + len));
    }

    // Check if we have enough remaining space
    ensure(len);

    // Copy data
    System.arraycopy(src, at, this.chars, to, len);
    to += len;
  }

  /**
   * Write to a char array.
   *
   * @param dst the char array destination
   * @param at the first index of the chars to write
   * @param len the amount of chars to write
   * @return the amount of chars written
   * @throws NullPointerException if the destintation is null
   * @throws IllegalArgumentException if the len argument is null
   * @throws IndexOutOfBoundsException if the destination is addressed out of its bounds
   */
  public int writeTo(char[] dst, int at, int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException  {
    if (dst == null) {
      throw new NullPointerException("Destination cannot be null");
    }
    if (at < 0) {
      throw new IndexOutOfBoundsException("Begin pointer cannot be negative: " + at);
    }
    if (len < 0) {
      throw new IllegalArgumentException("Length cannot be negative: " + len);
    }
    if (at + len > dst.length) {
      throw new IndexOutOfBoundsException("End pointer cannot be greater than buffer: " + (at + len));
    }

    // Determine how much we copy
    int l = Math.min(to - from, len);
    System.arraycopy(this.chars, from, dst, at, l);
    from += l;
    return l;
  }

  /**
   * Write to an appendable.
   *
   * @param dst the appendable destination
   * @return the amount of chars written
   * @throws NullPointerException if the destination is null
   * @throws IOException any IOException thrown by the <code>dst</code> argument
   */
  public int writeTo(Appendable dst) throws NullPointerException, IOException {
    if (dst == null) {
      throw new NullPointerException("Destination cannot be null");
    }

    //
    int from = this.from;
    int to = this.to;
    char[] chars = this.chars;

    //
    final int delta = to - from;
    if (delta > 0) {
      while (from < to) {
        dst.append(chars[from++]);
      }
      this.from = from;
      this.to = to;
      this.chars = chars;
    }

    //
    return delta;
  }

  private void ensure(int amount) {
    int remaining = chars.length - to;
    if (remaining < amount) {
      char[] tmp = new char[chars.length * 2];
      System.arraycopy(chars, from, tmp, from, to - from);
      this.chars = tmp;
    }
  }

  // Appendable implementation *****************************************************************************************

  public Appendable append(CharSequence csq) throws IOException {
    return append(csq, 0, csq.length());
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    if (start < 0) {
      throw new IndexOutOfBoundsException("Start " + start + " cannot be negative");
    }
    if (start > end) {
      throw new IndexOutOfBoundsException("Start " + start + " cannot be greater then end " + end);
    }
    if (end > csq.length()) {
      throw new IndexOutOfBoundsException("End " + end + " cannot be greater then char sequence " + csq.length());
    }
    int len = end - start;
    ensure(len);
    while (start < end) {
      chars[to++] = csq.charAt(start++);
    }
    return this;
  }

  public Appendable append(char c) throws IOException {
    ensure(1);
    chars[to++] = c;
    return this;
  }
}
