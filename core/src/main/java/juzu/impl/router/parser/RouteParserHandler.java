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
