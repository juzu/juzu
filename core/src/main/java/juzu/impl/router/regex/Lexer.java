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

import juzu.impl.common.CharStream;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Lexer {

  /** /. */
  private static final Pattern QUANTIFIER_PATTERN = Pattern.compile("^\\{([0-9]+)" + "(?:" + "(,)([0-9]+)?" + ")?\\}$");

  /** /. */
  private static final Pattern OCTAL_PATTERN = Pattern.compile("^[0-7]|[0-7][0-7]|[0-3][0-7][0-7]$");

  /** /. */
  private final CharStream stream;

  /** /. */
  private int ccDepth;

  /** /. */
  private Kind next;

  /** /. */
  private String token;

  /** /. */
  private Kind previous;

  Lexer(CharStream stream) {
    this.stream = stream;
    this.ccDepth = 0;
    this.next = null;
    this.previous = null;
    this.token = null;
  }

  Lexer(CharSequence seq) {
    this(new CharStream(seq));
  }

  int getIndex() {
    return stream.getIndex();
  }

  void reset() {
    this.stream.reset();
    this.ccDepth = 0;
    this.next = null;
    this.previous = null;
    this.token = null;
  }

  String getToken() {
    return token;
  }

  boolean isDone() {
    return !stream.hasNext();
  }

  boolean hasNext() throws SyntaxException {
    if (next == null) {
      if (stream.hasNext()) {
        Kind kind;
        char c = stream.next();
        switch (c) {
          case '^':
            kind = Kind.BEGIN;
            token = "^";
            break;
          case '$':
            kind = Kind.END;
            token = "$";
            break;
          case '.':
            kind = Kind.ANY;
            token = ".";
            break;
          case '-':
            if (ccDepth > 0) {
              if (stream.hasNext(']')) {
                kind = Kind.LITERAL;
                token = "-";
              }
              else if (previous == Kind.CC_OPEN) {
                kind = Kind.LITERAL;
                token = "-";
              }
              else if (previous == Kind.HYPHEN) {
                kind = Kind.LITERAL;
                token = "-";
              }
              else {
                kind = Kind.HYPHEN;
                token = "-";
              }
            }
            else {
              kind = Kind.LITERAL;
              token = "-";
            }
            break;
          case '|':
            kind = Kind.OR;
            token = "|";
            break;
          case '[': {

            kind = Kind.CC_OPEN;
            if (stream.next('^')) {
              token = "[^";
            }
            else {
              token = "[";
            }
            ccDepth++;
            break;
          }
          case ']':
            if (ccDepth > 0) {
              if (previous == Kind.CC_OPEN) {
                kind = Kind.LITERAL;
                token = "]";
              }
              else {
                kind = Kind.CC_CLOSE;
                token = "]";
                ccDepth--;
              }
            }
            else {
              kind = Kind.LITERAL;
              token = "]";
            }
            break;
          case '&':
            if (stream.next('&')) {
              kind = Kind.CC_AND;
              token = "&&";
            }
            else {
              kind = Kind.LITERAL;
              token = "&";
            }
            break;
          case '\\': {
            if (stream.hasNext()) {
              c = stream.peek();
              if (c == '0') {
                StringBuilder sb = new StringBuilder().append(stream.next());
                Character matched = null;
                while (true) {
                  if (stream.hasNext()) {
                    sb.append(stream.peek());
                    Matcher matcher = OCTAL_PATTERN.matcher(sb);
                    if (matcher.matches()) {
                      matched = (char)Integer.parseInt(sb.toString(), 8);
                      stream.next();
                    }
                    else {
                      break;
                    }
                  }
                  else {
                    break;
                  }
                }
                if (matched != null) {
                  kind = Kind.LITERAL;
                  token = Character.toString(matched);
                }
                else {
                  throw new SyntaxException();
                }
              }
              else if (c == 'x') {
                stream.next();
                if (stream.has(1)) {
                  String s = "" + stream.next() + stream.next();
                  try {
                    kind = Kind.LITERAL;
                    token = Character.toString((char)Integer.parseInt(s, 16));
                  }
                  catch (NumberFormatException e) {
                    throw new SyntaxException();
                  }
                }
                else {
                  throw new SyntaxException();
                }
              }
              else if (c == 'u') {
                stream.next();
                if (stream.has(3)) {
                  String s = "" + stream.next() + stream.next() + stream.next() + stream.next();
                  try {
                    kind = Kind.LITERAL;
                    token = Character.toString((char)Integer.parseInt(s, 16));
                  }
                  catch (NumberFormatException e) {
                    throw new SyntaxException();
                  }
                }
                else {
                  throw new SyntaxException();
                }
              }
              else if (Character.isLetterOrDigit(c)) {
                throw new SyntaxException();
              }
              else {
                stream.next();
                kind = Kind.LITERAL;
                token = "" + c;
              }
            }
            else {
              throw new SyntaxException();
            }
            break;
          }
          case '(': {
            if (ccDepth == 0) {
              StringBuilder sb = new StringBuilder("(");
              if (stream.hasNext('?')) {
                if (stream.has(1, ')')) {
                  // Do nothing
                }
                else {
                  stream.next();
                  sb.append('?');
                  if (stream.hasNext(':') || stream.hasNext('=') || stream.hasNext('!')) {
                    sb.append(stream.next());
                  }
                  else if (stream.next('<')) {
                    sb.append('<');
                    if (stream.hasNext('=') || stream.hasNext('!')) {
                      sb.append(stream.next());
                    }
                    else {
                      throw new SyntaxException();
                    }
                  }
                  else {
                    throw new SyntaxException();
                  }
                }
              }
              kind = Kind.GROUP_OPEN;
              token = sb.toString();
            }
            else {
              kind = Kind.LITERAL;
              token = "(";
            }
            break;
          }
          case '?':
            if (previous == Kind.GROUP_OPEN) {
              kind = Kind.LITERAL;
              token = "?";
            }
            else if (previous == Kind.QUANTIFIER) {
              kind = Kind.QUANTIFIER_MODE;
              token = "?";
            }
            else {
              kind = Kind.QUANTIFIER;
              token = "?";
            }
            break;
          case '+':
            if (previous == Kind.QUANTIFIER) {
              kind = Kind.QUANTIFIER_MODE;
              token = "+";
            }
            else {
              kind = Kind.QUANTIFIER;
              token = "+";
              break;
            }
            break;
          case '*':
            kind = Kind.QUANTIFIER;
            token = "*";
            break;
          case '{': {
            if (ccDepth == 0) {
              StringBuilder sb = new StringBuilder("{");
              while (stream.hasNext()) {
                c = stream.next();
                sb.append(c);
                if (c == '}') {
                  break;
                }
              }
              if (QUANTIFIER_PATTERN.matcher(sb).matches()) {
                kind = Kind.QUANTIFIER;
                token = sb.toString();
              }
              else {
                throw new SyntaxException();
              }
            }
            else {
              kind = Kind.LITERAL;
              token = "{";
            }
            break;
          }
          case ')':
            if (ccDepth == 0) {
              kind = Kind.GROUP_CLOSE;
              token = ")";
            }
            else {
              kind = Kind.LITERAL;
              token = ")";
            }
            break;
          default:
            kind = Kind.LITERAL;
            token = "" + c;
            break;
        }
        next = kind;
      }
    }
    return next != null;
  }

  boolean next(Kind expected) throws SyntaxException {
    if (hasNext() && expected == next) {
      previous = next();
      next = null;
      return true;
    }
    return false;
  }

  Kind next() throws SyntaxException {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    previous = next;
    next = null;
    return previous;
  }
}
