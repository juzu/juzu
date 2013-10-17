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
  public Iterable<String> getNames(Context path) {
    return path.getPath();
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
