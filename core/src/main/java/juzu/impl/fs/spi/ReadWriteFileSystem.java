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

package juzu.impl.fs.spi;

import juzu.impl.common.Content;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ReadWriteFileSystem<P> extends ReadFileSystem<P> {

  public final P makeFile(Iterable<String> path, String name) throws IOException {
    return makeFile(getRoot(), path, name);
  }

  public final P makeFile(P dir, Iterable<String> path, String name) throws IllegalArgumentException, IOException {
    dir = makeDir(dir, path);
    P child = getChild(dir, name);
    if (child == null) {
      return addFile(dir, name);
    }
    else if (isFile(child)) {
      return child;
    }
    else {
      throw new UnsupportedOperationException("handle me gracefully");
    }
  }

  public final P makeDir(Iterable<String> path) throws IOException {
    return makeDir(getRoot(), path);
  }

  public final P makeDir(P dir, Iterable<String> path) throws IllegalArgumentException, IOException {
    if (!isDir(dir)) {
      throw new IllegalArgumentException("Dir is not an effective dir");
    }
    for (String name : path) {
      P child = getChild(dir, name);
      if (child == null) {
        dir = addDir(dir, name);
      }
      else if (isDir(child)) {
        dir = child;
      }
      else {
        throw new UnsupportedOperationException("handle me gracefully");
      }
    }
    return dir;
  }

  public abstract P addDir(P parent, String name) throws IOException;

  public abstract P addFile(P parent, String name) throws IOException;

  public abstract void setContent(P file, Content content) throws IOException;

  public abstract void removePath(P path) throws IOException;


}
