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

package juzu.impl.compiler.file;

import juzu.impl.common.FileKey;
import juzu.impl.fs.spi.ReadFileSystem;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompositeFileManager extends FileManager {

  /** . */
  private FileManager[] components;

  public CompositeFileManager(StandardLocation location, Collection<ReadFileSystem<?>> fsList) {
    FileManager[] components = new FileManager[fsList.size()];
    int index = 0;
    for (ReadFileSystem<?> fs : fsList) {
      components[index++] = SimpleFileManager.wrap(location, fs);
    }

    //
    this.components = components;
  }

  @Override
  public void populateRoots(Set<File> roots) throws IOException {
    for (FileManager component : components) {
      component.populateRoots(roots);
    }
  }

  @Override
  public JavaFileObject getReadable(FileKey key) throws IOException {
    for (FileManager component : components) {
      JavaFileObject readable = component.getReadable(key);
      if (readable != null) {
        return readable;
      }
    }
    return null;
  }

  @Override
  public JavaFileObject getWritable(FileKey key) throws IOException {
    for (FileManager component : components) {
      JavaFileObject writable = component.getWritable(key);
      if (writable != null) {
        return writable;
      }
    }
    return null;
  }

  @Override
  public <C extends Collection<JavaFileObject>> C list(String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse, C to) throws IOException {
    for (FileManager component : components) {
      component.list(packageName, kinds, recurse, to);
    }
    return to;
  }

  @Override
  public void clearCache() {
    for (FileManager component : components) {
      component.clearCache();
    }
  }
}
