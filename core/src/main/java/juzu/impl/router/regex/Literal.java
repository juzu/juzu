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
