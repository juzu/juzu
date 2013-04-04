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

import juzu.impl.common.Location;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SyntaxException extends Exception {

  /** . */
  private final int code;

  /** . */
  private final Location location;

  public SyntaxException(int code, Location location) {
    this.code = code;
    this.location = location;
  }

  public SyntaxException(int code, String message, Location location) {
    super(message);
    this.code = code;
    this.location = location;
  }

  public SyntaxException(int code, String message, Throwable cause, Location location) {
    super(message, cause);
    this.code = code;
    this.location = location;
  }

  public SyntaxException(int code, Throwable cause, Location location) {
    super(cause);
    this.code = code;
    this.location = location;
  }

  public SyntaxException() {
    this(-1, (Location)null);
  }

  public SyntaxException(String s) {
    this(-1, s, (Location)null);
  }

  public SyntaxException(String s, Throwable throwable) {
    this(-1, s, throwable, null);
  }

  public SyntaxException(Throwable throwable) {
    this(-1, throwable, null);
  }

  public int getCode() {
    return code;
  }

  public Location getLocation() {
    return location;
  }
}
