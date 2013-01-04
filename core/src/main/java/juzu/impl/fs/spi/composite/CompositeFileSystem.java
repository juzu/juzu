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

import juzu.impl.common.Content;
import juzu.impl.common.Timestamped;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompositeFileSystem extends ReadFileSystem<Context> {

  /** . */
  private final Context root;

  /** . */
  final ReadFileSystem<?>[] compounds;

  public CompositeFileSystem(ReadFileSystem<?>... compounds) {
    this.compounds = compounds.clone();
    this.root = new Context(this);
  }

  @Override
  public Class<Context> getType() {
    return Context.class;
  }

  @Override
  public boolean equals(Context left, Context right) {
    return left == right;
  }

  @Override
  public Context getRoot() throws IOException {
    return root;
  }

  @Override
  public Context getChild(Context dir, String name) throws IOException {
    return dir.resolve().get(name);
  }

  @Override
  public Iterator<Context> getChildren(Context dir) throws IOException {
    return dir.resolve().getEntries();
  }

  @Override
  public long getLastModified(Context path) throws IOException {
    return 0;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getName(Context path) {
    return path.getKey();
  }

  @Override
  public PathType typeOf(Context path) throws IOException {
    for (int i = 0;i < path.paths.length;i++) {
      ReadFileSystem compound = compounds[i];
      Object p = path.paths[i];
      if (p != null) {
        PathType type = compound.typeOf(p);
        if (type != null) {
          return type;
        }
      }
    }
    return null;
  }

  @Override
  public Timestamped<Content> getContent(Context file) throws IOException {
    for (int i = 0;i < file.paths.length;i++) {
      ReadFileSystem compound = compounds[i];
      Object p = file.paths[i];
      if (p != null) {
        PathType type = compound.typeOf(p);
        if (type == PathType.FILE) {
          return compound.getContent(p);
        }
      }
    }
    throw new IOException("No content at " + file);
  }

  @Override
  public File getFile(Context path) {
    return null;
  }

  @Override
  public URL getURL(Context path) throws NullPointerException, IOException {
    return null;
  }
}
