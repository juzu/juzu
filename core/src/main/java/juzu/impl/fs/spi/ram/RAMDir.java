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

import java.util.LinkedHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class RAMDir extends RAMPath {

  /** . */
  final LinkedHashMap<String, RAMPath> children;

  /** . */
  long lastModified;

  RAMDir(RAMFileSystem fs) {
    super(fs);

    //
    this.children = new LinkedHashMap<String, RAMPath>();
    this.lastModified = System.currentTimeMillis();
  }

  RAMDir(RAMDir parent, String name, long lastModified) {
    super(parent, name);

    //
    this.children = new LinkedHashMap<String, RAMPath>();
    this.lastModified = lastModified;
  }

  @Override
  long getLastModified() {
    return lastModified;
  }

  @Override
  void touch() {
    this.lastModified = System.currentTimeMillis();
  }
}
