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
public class Location implements Serializable {

  public static Location at(int col, int line) {
    return new Location(col, line);
  }

  public static Location at(int index) {
    return new Location(index, 1);
  }

  /** . */
  private final int col;

  /** . */
  private final int line;

  public Location(int col, int line) {
    if (col < 0) {
      throw new IllegalArgumentException();
    }
    if (line < 0) {
      throw new IllegalArgumentException();
    }

    //
    this.col = col;
    this.line = line;
  }

  public int getCol() {
    return col;
  }

  public int getLine() {
    return line;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Location) {
      Location that = (Location)obj;
      return col == that.col && line == that.line;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Location[col=" + col + ",line=" + line + "]";
  }
}
