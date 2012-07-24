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

package juzu.impl.compiler.file;

import juzu.impl.fs.spi.SimpleFileSystem;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompositeFileManager extends FileManager {

  /** . */
  private FileManager[] components;

  public CompositeFileManager(Collection<SimpleFileSystem<?>> fsList) {
    FileManager[] components = new FileManager[fsList.size()];
    int index = 0;
    for (SimpleFileSystem<?> fs : fsList) {
      components[index++] = SimpleFileManager.wrap(fs);
    }

    //
    this.components = components;
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
