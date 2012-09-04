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
