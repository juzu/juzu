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

import juzu.impl.router.regex.Literal;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PatternBuilder {

  /** . */
  private final StringBuilder buffer = new StringBuilder();

  /** . */
  PatternBuilder() {
  }

  public PatternBuilder expr(CharSequence s) {
    if (s == null) {
      throw new NullPointerException("No null expression allowed");
    }
    buffer.append(s);
    return this;
  }

  public PatternBuilder expr(char s) {
    buffer.append(s);
    return this;
  }

  public PatternBuilder litteral(CharSequence s, int from, int to) {
    if (from < 0) {
      throw new IllegalArgumentException("No negative from argument");
    }
    if (to > s.length()) {
      throw new IllegalArgumentException("No to argument greater than the string length");
    }
    if (from > to) {
      throw new IllegalArgumentException("The to argument cannot be greater than the from argument");
    }
    if (from < to) {
      for (int i = from;i < to;i++) {
        char c = s.charAt(i);
        if (Literal.isEscaped(c)) {
          buffer.append('\\');
        }
        buffer.append(c);
      }
    }
    return this;
  }

  public PatternBuilder literal(CharSequence s, int from) {
    return litteral(s, from, s.length());
  }

  public PatternBuilder literal(CharSequence s) {
    return litteral(s, 0, s.length());
  }

  public PatternBuilder literal(char c) {
    return literal(Character.toString(c));
  }

  public PatternBuilder clear() {
    buffer.setLength(0);
    return this;
  }

  public String build() {
    return buffer.toString();
  }
}
