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
