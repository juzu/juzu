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

import juzu.impl.router.regex.SyntaxException;
import juzu.impl.common.Location;

/**
 * An hand crafted parser for the compact route syntax.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteParser {

  /** . */
  public static final int CODE_INVALID_EQUALS_CHAR = 0;

  /** . */
  public static final int CODE_INVALID_QUESTION_MARK_CHAR = 1;

  /** . */
  public static final int CODE_INVALID_AMPERSAND_CHAR = 2;

  /** . */
  public static final int CODE_UNCLOSED_EXPR = 3;

  /** . */
  public static final int CODE_EMPTY_REGEX = 4;

  /** . */
  public static final int CODE_UNCLOSED_REGEX = 5;

  /** . */
  public static final int CODE_MISSING_EXPR_IDENT = 6;

  /** . */
  public static final int CODE_UNCLOSED_MODIFIER = 7;

  /** . */
  private static final int READ_PATH = 0;

  /** . */
  private static final int READ_SEGMENT = 1;

  /** . */
  private static final int READ_QUESTION_MARK = 3;

  /** . */
  private static final int READ_QUERY_LHS = 4;

  /** . */
  private static final int READ_QUERY_RHS = 5;

  /** . */
  private static final int READ_AMPERSAND = 6;

  /** . */
  private static final int READ_DONE = 7;

  /** . */
  private static final int EXPR_BEGIN = 0;

  /** . */
  private static final int EXPR_IDENT = 1;

  /** . */
  private static final int EXPR_REGEX = 2;

  /** . */
  private static final int EXPR_AFTER_REGEX = 3;

  /** . */
  private static final int EXPR_MODIFIERS = 4;

  public static void parse(CharSequence s, RouteParserHandler handler) throws SyntaxException {
    parse(s, 0, s.length(), handler);
  }

  private static void parse(CharSequence s, int from, final int to, RouteParserHandler handler) throws SyntaxException {

    int status = READ_PATH;

    int pos = 0;

    while (true) {

      //
      switch (status) {
        case READ_PATH: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '/') {
              from++;
            } else if (c == '?') {
              handler.closePath();
              status = READ_QUESTION_MARK;
            } else {
              handler.openSegment();
              status = READ_SEGMENT;
              pos = from;
            }
          } else {
            handler.closePath();
            status = READ_DONE;
          }
          break;
        }
        case READ_SEGMENT: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '{') {
              if (from - pos > 0) {
                handler.segmentChunk(s, pos, from);
              }
              handler.openExpr();
              from = pos = parseExpr(s, from + 1, to, handler);
              handler.closeExpr();
            } else if (c == '/') {
              status = READ_PATH;
              if (from - pos > 0) {
                handler.segmentChunk(s, pos, from);
              }
              handler.closeSegment();
            } else if (c == '?') {
              if (from - pos > 0) {
                handler.segmentChunk(s, pos, from);
              }
              handler.closeSegment();
              handler.closePath();
              status = READ_QUESTION_MARK;
            } else {
              ++from;
            }
          } else {
            if (from - pos > 0) {
              handler.segmentChunk(s, pos, from);
            }
            handler.closeSegment();
            status = READ_PATH;
            from++;
          }
          break;
        }
        case READ_QUESTION_MARK: {
          handler.query();
          pos = ++from;
          status = READ_QUERY_LHS;
          break;
        }
        case READ_QUERY_LHS: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '=') {
              if (from - pos > 0) {
                handler.queryParamLHS(s, pos, from);
                handler.queryParamRHS();
                pos = ++from;
                status = READ_QUERY_RHS;
              } else {
                throw new SyntaxException(CODE_INVALID_EQUALS_CHAR, "Invalid char = at index " + from, Location.at(1 + from));
              }
            } else if (c == '?') {
              throw new SyntaxException(CODE_INVALID_QUESTION_MARK_CHAR, "Invalid char ? at index " + from, Location.at(1 + from));
            } else if (c == '&') {
              if (from - pos > 0) {
                handler.queryParamLHS(s, pos, from);
                handler.endQueryParam();
                pos = ++from;
              } else {
                throw new SyntaxException(CODE_INVALID_AMPERSAND_CHAR, "Invalid char & at index " + from, Location.at(1 + from));
              }
            } else {
              from++;
            }
          } else {
            if (from - pos > 0) {
              handler.queryParamLHS(s, pos, from);
              handler.endQueryParam();
            }
            status = READ_DONE;
          }
          break;
        }
        case READ_QUERY_RHS: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '&') {
              if (from - pos > 0) {
                handler.queryParamRHS(s, pos, from);
              }
              handler.endQueryParam();
              pos = ++from;
              status = READ_QUERY_LHS;
            } else if (c == '{') {
              if (from - pos == 0) {
                handler.openExpr();
                from = pos = parseExpr(s, ++from, to, handler);
                handler.closeExpr();
                handler.endQueryParam();
                status = READ_QUERY_LHS;
              } else {
                from++;
              }
            } else {
              from++;
            }
          } else {
            handler.queryParamRHS(s, pos, from);
            handler.endQueryParam();
            status = READ_DONE;
          }
          break;
        }
        case READ_AMPERSAND: {
          if (from < to) {
            pos = from++;
            status = READ_QUERY_LHS;
          } else {
            status = READ_DONE;
          }
          break;
        }
        case READ_DONE:
          return;
        default:
          throw new UnsupportedOperationException("Need to implement status " + status);
      }
    }
  }

  private static int parseExpr(CharSequence s, int from, int to, RouteParserHandler handler) throws SyntaxException {

    int status = EXPR_BEGIN;
    int pos = from;

    while (true) {
      switch (status) {
        case EXPR_BEGIN: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '}') {
              handler.ident(s, pos, from);
              return ++from;
            } else {
              if (c == '<') {
                status = EXPR_REGEX;
                pos = ++from;
              } else if (c == '[') {
                status = EXPR_MODIFIERS;
                pos = ++from;
              } else {
                status = EXPR_IDENT;
              }
              break;
            }
          } else {
            throw new SyntaxException(CODE_UNCLOSED_EXPR, "Unclosed expression at " + from, Location.at(1 + from));
          }
        }
        case EXPR_IDENT: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '}') {
              handler.ident(s, pos, from);
              return ++from;
            } else {
              ++from;
              break;
            }
          } else {
            throw new SyntaxException(CODE_UNCLOSED_EXPR, "Unclosed expression at " + from, Location.at(1 + from));
          }
        }
        case EXPR_REGEX: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '>') {
              if (from - pos > 0) {
                handler.pattern(s, pos, from);
                pos = ++from;
                status = EXPR_AFTER_REGEX;
                break;
              } else {
                throw new SyntaxException(CODE_EMPTY_REGEX, "Regular expression cannot be empty expression at " + from, Location.at(1 + from));
              }
            } else {
              from++;
              break;
            }
          } else {
            throw new SyntaxException(CODE_UNCLOSED_REGEX, "Unclosed regular expression  at " + from, Location.at(1 + from));
          }
        }
        case EXPR_AFTER_REGEX: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == '}') {
              throw new SyntaxException(CODE_MISSING_EXPR_IDENT, "Expression must have an identifier  at " + from, Location.at(1 + from));
            } else {
              if (c == '[') {
                pos = ++from;
                status = EXPR_MODIFIERS;
              } else {
                pos = from;
                status = EXPR_IDENT;
              }
              break;
            }
          } else {
            throw new SyntaxException(CODE_MISSING_EXPR_IDENT, "Expression must have an identifier  at " + from, Location.at(1 + from));
          }
        }
        case EXPR_MODIFIERS: {
          if (from < to) {
            char c = s.charAt(from);
            if (c == ']') {
              handler.modifiers(s, pos, from);
              status = EXPR_IDENT;
              pos = ++from;
              break;
            } else {
              from++;
            }
            break;
          } else {
            throw new SyntaxException(CODE_UNCLOSED_MODIFIER, "Unclosed modifier at " + from, Location.at(1 + from));
          }
        }
        default:
          throw new UnsupportedOperationException("todo");
      }
    }
  }
}
