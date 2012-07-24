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

package juzu.impl.common;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Coordinate implements Serializable {

  /** . */
  private final int offset;

  /** . */
  private final Location position;

  public Coordinate(int offset, Location position) {
    this.offset = offset;
    this.position = position;
  }

  public Coordinate(int offset, int col, int line) {
    this.offset = offset;
    this.position = new Location(col, line);
  }

  public int getOffset() {
    return offset;
  }

  public Location getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[offset=" + offset + ",position=" + position + "]";
  }
}
