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

package juzu.impl.router.parser;

import juzu.impl.router.regex.SyntaxException;
import juzu.impl.common.Location;

/**
 * An hand crafted parser for the compact route syntax.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteParser {

  /** . */
  public static final int CODE_UNCLOSED_EXPR = 0;

  /** . */
  public static final int CODE_MISSING_EXPR_IDENT = 1;

  /** . */
  private static final int READ_PATH = 0;

  /** . */
  private static final int READ_SEGMENT = 1;

  /** . */
  private static final int READ_DONE = 7;

  /** . */
  private static final int EXPR_BEGIN = 0;

  /** . */
  private static final int EXPR_IDENT = 1;

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
            } else {
              handler.segmentOpen();
              status = READ_SEGMENT;
              pos = from;
            }
          } else {
            boolean slash = from > pos && s.charAt(from - 1) == '/';
            handler.pathClose(slash);
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
              handler.exprOpen();
              from = pos = parseExpr(s, from + 1, to, handler);
              handler.exprClose();
            } else if (c == '/') {
              status = READ_PATH;
              if (from - pos > 0) {
                handler.segmentChunk(s, pos, from);
              }
              handler.segmentClose();
            } else {
              ++from;
            }
          } else {
            if (from - pos > 0) {
              handler.segmentChunk(s, pos, from);
            }
            handler.segmentClose();
            status = READ_PATH;
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
              throw new SyntaxException(CODE_MISSING_EXPR_IDENT, "Missing expression identifier at " + from, Location.at(1 + from));
            } else {
              status = EXPR_IDENT;
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
              handler.exprIdent(s, pos, from);
              return ++from;
            } else {
              ++from;
              break;
            }
          } else {
            throw new SyntaxException(CODE_UNCLOSED_EXPR, "Unclosed expression at " + from, Location.at(1 + from));
          }
        }
        default:
          throw new UnsupportedOperationException("todo");
      }
    }
  }
}
