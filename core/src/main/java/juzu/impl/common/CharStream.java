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

import java.util.NoSuchElementException;

/**
 * Wraps a {@link CharSequence} to become a stream of chars.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CharStream {

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  /**
   * Create a new stream of the provided sequence.
   *
   * @param s the sequence
   * @throws NullPointerException if the sequence is null
   */
  public CharStream(CharSequence s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException("No null sequence accepted");
    }

    //
    this.s = s;
    this.index = 0;
  }

  public int getIndex() {
    return index;
  }

  public void reset() {
    index = 0;
  }

  public boolean hasNext() {
    return has(0);
  }

  public boolean hasNext(char c) {
    return has(0, c);
  }

  public boolean has(int delta) {
    return index + delta < s.length();
  }

  public boolean has(int delta, char c) {
    int offset = index + delta;
    return offset < s.length() && s.charAt(offset) == c;
  }

  public boolean next(char c) {
    boolean a = index < s.length() && s.charAt(index) == c;
    if (a) {
      index++;
    }
    return a;
  }

  public Character peek() {
    return peek(0);
  }

  public Character peek(int delta) {
    int offset = index + delta;
    if (offset < s.length()) {
      return s.charAt(offset);
    }
    else {
      return null;
    }
  }

  public char next() throws NoSuchElementException {
    if (hasNext()) {
      return s.charAt(index++);
    }
    else {
      throw new NoSuchElementException();
    }
  }
}
