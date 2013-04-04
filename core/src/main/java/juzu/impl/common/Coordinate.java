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
