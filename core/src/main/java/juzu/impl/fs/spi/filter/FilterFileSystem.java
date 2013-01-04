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

package juzu.impl.fs.spi.filter;

import juzu.UndeclaredIOException;
import juzu.impl.common.Content;
import juzu.impl.common.Timestamped;
import juzu.impl.fs.Filter;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FilterFileSystem<P> extends ReadFileSystem<P> {

  /** . */
  private final ReadFileSystem<P> filtered;

  /** . */
  private final Filter<P> filter;

  public FilterFileSystem(ReadFileSystem<P> filtered, Filter<P> filter) {
    this.filtered = filtered;
    this.filter = filter;
  }

  @Override
  public boolean equals(P left, P right) {
    return filtered.equals(left, right);
  }

  @Override
  public P getRoot() throws IOException {
    P root = filtered.getRoot();
    String name = filtered.getName(root);
    if (filter.acceptDir(root, name)) {
      return root;
    } else {
      throw new IOException("Cannot access root");
    }
  }

  @Override
  public P getChild(P dir, String name) throws IOException {
    P child = filtered.getChild(dir, name);
    return child == null ? null : get(name, child);
  }

  @Override
  public long getLastModified(P path) throws IOException {
    return filtered.getLastModified(path);
  }

  @Override
  public Class<P> getType() {
    return filtered.getType();
  }

  @Override
  public String getDescription() {
    return "Filter[" + filtered.getDescription() + "]";
  }

  @Override
  public String getName(P path) {
    return filtered.getName(path);
  }

  @Override
  public Iterator<P> getChildren(P dir) throws IOException {
    final Iterator<P> i = filtered.getChildren(dir);
    return new Iterator<P>() {
      P next;
      public boolean hasNext() {
        try {
          while (next == null && i.hasNext()) {
            P path = i.next();
            String name = filtered.getName(path);
            next = get(name, path);
          }
          return next != null;
        }
        catch (IOException e) {
          throw new UndeclaredIOException(e);
        }
      }
      public P next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        P path = next;
        next = null;
        return path;
      }
      public void remove() {
        throw new UnsupportedClassVersionError();
      }
    };
  }

  @Override
  public PathType typeOf(P path) throws IOException {
    return filtered.typeOf(path);
  }

  @Override
  public Timestamped<Content> getContent(P file) throws IOException {
    return filtered.getContent(file);
  }

  @Override
  public File getFile(P path) {
    return filtered.getFile(path);
  }

  @Override
  public URL getURL(P path) throws NullPointerException, IOException {
    return filtered.getURL(path);
  }

  private P get(String name, P child) throws IOException {
    if (filtered.isDir(child)) {
      if (filter.acceptDir(child, name)) {
        return child;
      } else {
        return null;
      }
    } else {
      if (filter.acceptFile(child, name)) {
        return child;
      } else {
        return null;
      }
    }
  }
}
