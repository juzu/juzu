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

  public final P makePath(Iterable<String> path) throws IOException {
    return makePath(getRoot(), path);
  }

  public final P makePath(P dir, Iterable<String> path) throws IllegalArgumentException, IOException {
    if (!isDir(dir)) {
      throw new IllegalArgumentException("Dir is not an effective dir");
    }
    for (String name : path) {
      dir = makePath(dir, name);
    }
    return dir;
  }

  /**
   * Create and return a new path representation.
   *
   * @param parent the parent path
   * @param name the path name
   * @return the path
   * @throws IOException any io exception
   */
  public abstract P makePath(P parent, String name) throws IOException;

  public abstract void createDir(P dir) throws IOException;

  public abstract long setContent(P file, Content content) throws IOException;

  public abstract boolean removePath(P path) throws IOException;


}
