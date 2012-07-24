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

package juzu.plugin.less.impl.lesser;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessError extends Result {

  /** . */
  public final String src;

  /** . */
  public final int line;

  /** . */
  public final int column;

  /** . */
  public final int index;

  /** . */
  public final String message;

  /** . */
  public final String type;

  /** . */
  public final String[] extract;

  public LessError(String src, int line, int column, int index, String message, String type, String[] extract) {
    this.src = src;
    this.line = line;
    this.column = column;
    this.index = index;
    this.message = message;
    this.type = type;
    this.extract = extract;
  }

  @Override
  public String toString() {
    return "Failure[src=" + src + ",line=" + line + ",column=" + column + ",index=" + index + ",message=" + message + ",type=" +
      type + ",extract=" + Arrays.asList(extract) + "]";
  }
}
