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

import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.fs.spi.SimpleFileSystem;
import juzu.impl.common.Spliterator;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SimpleFileManager<P> extends FileManager {

  public static <P> SimpleFileManager<P> wrap(SimpleFileSystem<P> fs) {
    return new SimpleFileManager<P>(fs);
  }

  /** . */
  final SimpleFileSystem<P> fs;

  /** . */
  final Map<FileKey, JavaFileObjectImpl<P>> entries;

  public SimpleFileManager(SimpleFileSystem<P> fs) {
    this.fs = fs;
    this.entries = new HashMap<FileKey, JavaFileObjectImpl<P>>();
  }

  public SimpleFileSystem<P> getFileSystem() {
    return fs;
  }

  public void clearCache() {
    entries.clear();
  }

  public JavaFileObject getReadable(FileKey key) throws IOException {
    JavaFileObjectImpl<P> entry = entries.get(key);
    if (entry == null) {
      P file = fs.getPath(key.names);
      if (file != null && fs.isFile(file)) {
        entries.put(key, entry = new JavaFileObjectImpl<P>(key, this, file));
      }
    }
    return entry;
  }

  public JavaFileObject getWritable(FileKey key) throws IOException {
    if (fs instanceof ReadWriteFileSystem<?>) {
      ReadWriteFileSystem<P> rwFS = (ReadWriteFileSystem<P>)fs;
      JavaFileObjectImpl<P> entry = entries.get(key);
      if (entry == null) {
        P file = rwFS.getPath(key.names);
        entries.put(key, entry = new JavaFileObjectImpl<P>(key, this, file));
      }
      return entry;
    }
    else {
      throw new UnsupportedOperationException("File system is not writable");
    }
  }

  @Override
  public <C extends Collection<JavaFileObject>> C list(
    String packageName,
    Set<JavaFileObject.Kind> kinds,
    boolean recurse,
    C to) throws IOException {
    Iterable<String> packageNames = Spliterator.split(packageName, '.');
    P dir = fs.getPath(packageNames);
    if (dir != null && fs.isDir(dir)) {
      list(dir, kinds, recurse, to);
    }
    return to;
  }

  private void list(
    P root,
    Set<JavaFileObject.Kind> kinds,
    boolean recurse,
    Collection<JavaFileObject> to) throws IOException {
    StringBuilder sb = new StringBuilder();
    fs.packageOf(root, '.', sb);
    String packageName = sb.toString();
    for (Iterator<P> i = fs.getChildren(root);i.hasNext();) {
      P child = i.next();
      if (fs.isDir(child)) {
        if (recurse) {
          list(child, kinds, true, to);
        }
      }
      else {
        String name = fs.getName(child);
        FileKey key = FileKey.newName(packageName, name);
        if (kinds.contains(key.kind)) {
          to.add(getReadable(key));
        }
      }
    }
  }
}
