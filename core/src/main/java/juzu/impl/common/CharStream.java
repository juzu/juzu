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
