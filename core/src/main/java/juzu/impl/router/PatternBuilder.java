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
