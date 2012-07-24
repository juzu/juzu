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

package juzu.impl.fs.spi.ram;

import juzu.impl.common.Content;

import java.util.LinkedHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMDir extends RAMPath {

  /** . */
  final LinkedHashMap<String, RAMPath> children;

  /** . */
  private long lastModified;

  public RAMDir() {
    this.children = new LinkedHashMap<String, RAMPath>();
    this.lastModified = System.currentTimeMillis();
  }

  public RAMDir(RAMDir parent, String name) {
    super(parent, name);

    //
    this.children = new LinkedHashMap<String, RAMPath>();
  }

  public RAMFile addFile(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.indexOf('/') != -1) {
      throw new IllegalArgumentException("Name must not container '/'");
    }
    if (children.containsKey(name)) {
      throw new IllegalStateException();
    }

    //
    RAMFile dir = new RAMFile(this, name);
    children.put(name, dir);
    return dir;
  }

  public RAMDir addDir(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.indexOf('/') != -1) {
      throw new IllegalArgumentException("Name must not container '/'");
    }
    if (children.containsKey(name)) {
      throw new IllegalStateException();
    }

    //
    RAMDir dir = new RAMDir(this, name);
    children.put(name, dir);
    return dir;
  }

  @Override
  public void touch() {
    this.lastModified = System.currentTimeMillis();
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  public RAMPath getChild(String name) {
    return children.get(name);
  }

  @Override
  public RAMFile update(Content content) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Content getContent() {
    return null;
  }

  public Iterable<RAMPath> getChildren() {
    return children.values();
  }

  public void clear() {
    children.clear();
  }
}
