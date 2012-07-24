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

package juzu.impl.fs.spi.jar;

import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Content;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JarFileSystem extends ReadFileSystem<JarPath> {

  /** . */
  final JarFile jar;

  /** . */
  final URL jarURL;

  /** A synthetic jar entry. */
  private final JarPath root;

  public JarFileSystem(JarFile f) throws IOException {

    //
    this.jar = f;
    this.jarURL = new File(f.getName()).toURI().toURL();

    //
    JarPath root = new JarPath(this);
    for (Enumeration<JarEntry> en = jar.entries();en.hasMoreElements();) {
      JarEntry entry = en.nextElement();
      root.append(entry);
    }

    //
    this.root = root;
  }

  @Override
  public String getDescription() {
    return "jar[" + jarURL + "]";
  }

  @Override
  public boolean equals(JarPath left, JarPath right) {
    return left == right;
  }

  @Override
  public JarPath getRoot() throws IOException {
    return root;
  }

  @Override
  public JarPath getParent(JarPath path) throws IOException {
    return path.parent;
  }

  @Override
  public String getName(JarPath path) throws IOException {
    return path.name;
  }

  @Override
  public Iterator<JarPath> getChildren(JarPath dir) throws IOException {
    if (isFile(dir)) {
      throw new IllegalArgumentException("Not a directory");
    }
    return dir.getChildren();
  }

  @Override
  public JarPath getChild(JarPath dir, String name) throws IOException {
    if (isFile(dir)) {
      throw new IllegalArgumentException("Not a directory");
    }
    return dir.getChild(name);
  }

  @Override
  public boolean isDir(JarPath path) throws IOException {
    return path.dir;
  }

  @Override
  public boolean isFile(JarPath path) throws IOException {
    return !isDir(path);
  }

  @Override
  public Content getContent(JarPath file) throws IOException {
    return file.getContent();
  }

  @Override
  public long getLastModified(JarPath path) throws IOException {
    return 0;
  }

  @Override
  public URL getURL(JarPath path) throws IOException {
    return path.getURL();
  }

  @Override
  public File getFile(JarPath path) throws IOException {
    return null;
  }
}
