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

import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Spliterator;
import juzu.impl.fs.spi.disk.DiskFileSystem;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SimpleFileManager<P> extends FileManager {

  public static <P> SimpleFileManager<P> wrap(ReadFileSystem<P> fs) {
    return new SimpleFileManager<P>(fs);
  }

  /** . */
  final ReadFileSystem<P> fs;

  /** . */
  final Map<FileKey, JavaFileObjectImpl<P>> entries;

  public SimpleFileManager(ReadFileSystem<P> fs) {
    this.fs = fs;
    this.entries = new HashMap<FileKey, JavaFileObjectImpl<P>>();
  }

  public ReadFileSystem<P> getFileSystem() {
    return fs;
  }

  @Override
  public void populateRoots(Set<File> roots) throws IOException {
    if (fs instanceof DiskFileSystem) {
      roots.add(((DiskFileSystem)fs).getRoot());
    }
  }

  public void clearCache() {
    entries.clear();
  }

  public JavaFileObject getReadable(FileKey key) throws IOException {
    JavaFileObjectImpl<P> entry = entries.get(key);
    if (entry == null) {
      P file = fs.getPath(key.names);
      if (file != null && fs.isFile(file)) {
        entries.put(key, entry = new JavaFileObjectImpl<P>(key, fs, file));
      }
    }
    return entry;
  }

  public JavaFileObject getWritable(FileKey key) throws IOException {
    if (fs instanceof ReadWriteFileSystem<?>) {
      ReadWriteFileSystem<P> rwFS = (ReadWriteFileSystem<P>)fs;
      JavaFileObjectImpl<P> entry = entries.get(key);
      if (entry == null) {
        P file = rwFS.makePath(key.names);
        entries.put(key, entry = new JavaFileObjectImpl<P>(key, fs, file));
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
      list(packageName, dir, kinds, recurse, to);
    }
    return to;
  }

  private void list(
    String packageName,
    P root,
    Set<JavaFileObject.Kind> kinds,
    boolean recurse,
    Collection<JavaFileObject> to) throws IOException {
    for (Iterator<P> i = fs.getChildren(root);i.hasNext();) {
      P child = i.next();
      if (fs.isDir(child)) {
        if (recurse) {
          String name = fs.getName(child);
          list(packageName.isEmpty() ? name : packageName + "." + name, child, kinds, true, to);
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
