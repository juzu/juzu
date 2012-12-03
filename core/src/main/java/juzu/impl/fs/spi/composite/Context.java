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
