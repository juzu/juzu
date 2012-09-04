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

/**
 * Handles {@link RouteParser} callbacks.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface RouteParserHandler {

  /**
   * Opens a segment.
   */
  void segmentOpen();

  /**
   * A segment chunck data.
   *
   * @param s the char sequence
   * @param from the position to read from (inclusive)
   * @param to the position to read to (exclusive)
   */
  void segmentChunk(CharSequence s, int from, int to);

  /**
   * Close the segment.
   */
  void segmentClose();

  /**
   * Close the path, this will be always called. When the path ends with a <code>/</code> the
   * <code>slash</code> argument is true. Note that if the route path is equals to <code>/</code>
   * the <code>slash</code> argument will be true.
   *
   * @param slash when the path ends with a <code>/</code>
   */
  void pathClose(boolean slash);

  /**
   * Opens an expression.
   */
  void exprOpen();

  /**
   * The expression identifier.
   *
   * @param s the char sequence
   * @param from the position to read from (inclusive)
   * @param to the position to read to (exclusive)
   */
  void exprIdent(CharSequence s, int from, int to);

  /**
   * Close the expressions.
   */
  void exprClose();

}
