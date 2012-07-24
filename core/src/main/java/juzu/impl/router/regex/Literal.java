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

package juzu.impl.router.regex;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Literal {
  /**
   * Return true if the char should be escaped. Note that the implementation sometime may escape the char although the
   * context in which it is used would not require its escape.
   *
   * @param c the char to test
   * @return true when the char should be escaped.
   */
  public static boolean isEscaped(char c) {
    return c >= '(' && c <= '+' // ()*+
        || c == '?'
        || c == '{'
        || c == '}'
        || c == '|'
        || c == '$'
        || c == '&'
        || c == '^'
        || c == '-'
        || c == '.'
        || c == '['
        || c == ']'
        || c == '\\';
  }

  /**
   * Return the char value as a string literal in a regexp. Note that the implementation does not tries to optimize the
   * value with respect to the AST context, (for instance (?) would be fine as (?) but it will rewritten as (\?).
   *
   * @param value the value
   * @return the escaped string
   */
  public String getEscape(char value) {
    switch (value) {
      case '|':
        return "\\|";
      case '&':
        return "\\&";
      case '$':
        return "\\$";
      case '^':
        return "\\^";
      case '-':
        return "\\-";
      case '.':
        return "\\.";
      case '?':
        return "\\?";
      case '+':
        return "\\+";
      case '*':
        return "\\*";
      case '[':
        return "\\[";
      case ']':
        return "\\]";
      case '(':
        return "\\(";
      case ')':
        return "\\)";
      case '{':
        return "\\{";
      case '}':
        return "\\}";
      case '\\':
        return "\\\\";
      default:
        return Character.toString(value);
    }
  }

  public static void escapeTo(char value, Appendable appendable) throws IOException, NullPointerException {
    if (appendable == null) {
      throw new NullPointerException();
    }
    if (isEscaped(value)) {
      appendable.append('\\');
    }
    appendable.append(value);
  }
}
