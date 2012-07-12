/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.router.regex;

import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Stream {

  /** . */
  private final CharSequence stream;

  /** . */
  private int index;

  public Stream(CharSequence stream) {
    this.stream = stream;
    this.index = 0;
  }

  public int getIndex() {
    return index;
  }

  public void reset() {
    index = 0;
  }

  public boolean hasNext() {
    return hasNext(0);
  }

  public boolean hasNext(int delta) {
    return index + delta < stream.length();
  }

  public boolean hasNext(char c) {
    return hasNext(0, c);
  }

  public boolean hasNext(int delta, char c) {
    int offset = index + delta;
    return offset < stream.length() && stream.charAt(offset) == c;
  }

  public boolean next(char c) {
    boolean a = index < stream.length() && stream.charAt(index) == c;
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
    if (offset < stream.length()) {
      return stream.charAt(offset);
    }
    else {
      return null;
    }
  }

  public char next() {
    if (hasNext()) {
      return stream.charAt(index++);
    }
    else {
      throw new NoSuchElementException();
    }
  }
}
