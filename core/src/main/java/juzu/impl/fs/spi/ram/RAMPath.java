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

package juzu.impl.fs.spi.ram;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class RAMPath {

  /** . */
  private static final String[] EMPTY = new String[0];

  /** . */
  final RAMFileSystem fs;

  /** . */
  final String name;

  /** . */
  final String[] names;

  /** . */
  RAMDir parent;

  RAMPath(RAMFileSystem fs) {
    this.fs = fs;
    this.name = "";
    this.parent = null;
    this.names = EMPTY;
  }

  RAMPath(RAMDir parent, String name) {
    if (parent == null) {
      throw new NullPointerException();
    }
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.length() == 0) {
      throw new IllegalArgumentException();
    }

    //
    String[] names = Arrays.copyOf(parent.names, parent.names.length + 1);
    names[names.length - 1] = name;

    //
    this.fs = parent.fs;
    this.name = name;
    this.parent = parent;
    this.names = names;
  }

  abstract long getLastModified();

  abstract void touch();

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + name + "]";
  }
}
