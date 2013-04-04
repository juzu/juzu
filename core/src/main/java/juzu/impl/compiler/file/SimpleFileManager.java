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
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Spliterator;
import juzu.impl.fs.spi.disk.DiskFileSystem;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SimpleFileManager<P> extends FileManager {

  public static <P> SimpleFileManager<P> wrap(StandardLocation location, ReadFileSystem<P> fs) {
    return new SimpleFileManager<P>(location, fs);
  }

  /** . */
  final StandardLocation location;

  /** . */
  final ReadFileSystem<P> fs;

  /** . */
  final Map<FileKey, JavaFileObjectImpl<P>> entries;

  public SimpleFileManager(StandardLocation location, ReadFileSystem<P> fs) {
    this.location = location;
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
        entries.put(key, entry = new JavaFileObjectImpl<P>(location, key, fs, file));
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
        entries.put(key, entry = new JavaFileObjectImpl<P>(location, key, fs, file));
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
        if (kinds.contains(key.getKind())) {
          to.add(getReadable(key));
        }
      }
    }
  }
}
