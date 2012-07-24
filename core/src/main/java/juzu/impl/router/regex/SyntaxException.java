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
