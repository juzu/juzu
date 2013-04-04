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

package juzu.impl.fs.spi.composite;

import juzu.impl.common.AbstractTrie;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;

import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Context extends AbstractTrie<String, Context> {

  /** . */
  final CompositeFileSystem fs;

  /** . */
  boolean resolved;

  /** . */
  final Object[] paths;

  Context(CompositeFileSystem fs) {
    this.fs = fs;
    this.resolved = false;
    this.paths = new Object[fs.compounds.length];
  }

  private Context(Context parent, String key) {
    super(parent, key);

    //
    this.fs = parent.fs;
    this.resolved = false;
    this.paths = new Object[parent.paths.length];
  }

  @Override
  protected Context create(Context parent, String key) {
    return new Context(parent, key);
  }

  Context resolve() throws IOException {
    if (!resolved) {

      // We must first resolve the roots of the root
      Context parent = getParent();
      if (parent == null) {
        for (int i = 0;i < paths.length;i++) {
          paths[i] = fs.compounds[i].getRoot();
        }
      }

      // Load children
      for (int i = 0;i < paths.length;i++) {
        Object path = paths[i];
        if (path != null) {
          ReadFileSystem compound = fs.compounds[i];
          if (compound.typeOf(path) == PathType.DIR) {
            Iterator children = compound.getChildren(path);
            while (children.hasNext()) {
              Object child = children.next();
              String name = compound.getName(child);
              add(name).paths[i] = child;
            }
          }
        }
      }

      //
      this.resolved = true;
    }

    //
    return this;
  }
}
