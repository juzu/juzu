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

package juzu.impl.fs.spi;

import juzu.impl.fs.Filter;
import juzu.impl.fs.Visitor;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File system provider interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ReadFileSystem<P> extends SimpleFileSystem<P> {

  public final void dump(Appendable appendable) throws IOException {
    dump(getRoot(), appendable);
  }

  public final void dump(P path, final Appendable appendable) throws IOException {
    final StringBuilder prefix = new StringBuilder();
    traverse(path, new Visitor<P>() {
      public void enterDir(P dir, String name) throws IOException {
        prefix.append(name).append('/');
      }

      public void file(P file, String name) throws IOException {
        appendable.append(prefix).append(name).append("\n");
      }

      public void leaveDir(P dir, String name) throws IOException {
        prefix.setLength(prefix.length() - 1 - name.length());
      }
    });
  }

  @Override
  public void packageOf(P path, Collection<String> to) throws IOException {
    if (equals(getRoot(), path)) {
      // Do nothing
    }
    else {
      P parent = getParent(path);
      packageOf(parent, to);
      if (isDir(path)) {
        String name = getName(path);
        to.add(name);
      }
    }
  }

  public final Content getContent(String... names) throws IOException {
    return getContent(Arrays.<String>asList(names));
  }

  public final Content getContent(Iterable<String> names) throws IOException {
    P path = getPath(names);
    if (path != null && isFile(path)) {
      return getContent(path);
    }
    else {
      return null;
    }
  }

  public final P getPath(Iterable<String> names) throws IOException {
    P current = getRoot();
    for (String name : names) {
      if (isDir(current)) {
        P child = getChild(current, name);
        if (child != null) {
          current = child;
        }
        else {
          return null;
        }
      }
      else {
        throw new UnsupportedOperationException("handle me gracefully : was expecting " + Tools.list(names) + " to resolve");
      }
    }
    return current;
  }

  public static final int DIR = 0;

  public static final int FILE = 1;

  public static final int PATH = 2;

  public final int size(final int mode) throws IOException {
    switch (mode) {
      case DIR:
      case PATH:
      case FILE:
        break;
      default:
        throw new IllegalArgumentException("Illegal mode " + mode);
    }
    final AtomicInteger size = new AtomicInteger();
    traverse(new Visitor.Default<P>() {
      @Override
      public void enterDir(P dir, String name) throws IOException {
        if (mode == PATH || mode == DIR) {
          size.incrementAndGet();
        }
      }

      @Override
      public void file(P file, String name) throws IOException {
        if (mode == PATH || mode == FILE) {
          size.incrementAndGet();
        }
      }
    });
    return size.get();
  }

  /** . */
  private static final Filter NULL = new Filter.Default();

  public final void traverse(Filter<P> filter, Visitor<P> visitor) throws IOException {
    traverse(getRoot(), filter, visitor);
  }

  public final void traverse(Visitor<P> visitor) throws IOException {
    traverse(getRoot(), visitor);
  }

  public final void traverse(P path, Visitor<P> visitor) throws IOException {
    // This is OK as it always return true
    @SuppressWarnings("unchecked")
    Filter<P> filter = NULL;
    traverse(path, filter, visitor);
  }

  public final void traverse(P path, Filter<P> filter, Visitor<P> visitor) throws IOException {
    String name = getName(path);
    if (isDir(path)) {
      if (filter.acceptDir(path, name)) {
        visitor.enterDir(path, name);
        for (Iterator<P> i = getChildren(path);i.hasNext();) {
          P child = i.next();
          traverse(child, filter, visitor);
        }
        visitor.leaveDir(path, name);
      }
    }
    else if (filter.acceptFile(path, name)) {
      visitor.file(path, name);
    }
  }

  public <D> void copy(ReadWriteFileSystem<D> dst) throws IOException {
    copy(new Filter.Default<P>(), dst);
  }

  public <D> void copy(Filter<P> filter, ReadWriteFileSystem<D> dst) throws IOException {
    copy(getRoot(), filter, dst, dst.getRoot());
  }

  public <D> void copy(P srcPath, Filter<P> filter, ReadWriteFileSystem<D> dst, D dstPath) throws IOException {
    int kind = kind(srcPath, dst, dstPath);
    String srcName = getName(srcPath);

    //
    switch (kind) {
      case 0: {
        if (filter.acceptFile(srcPath, srcName)) {
          dst.setContent(dstPath, getContent(srcPath));
        }
        break;
      }
      case 3: {
        // Inspect destination
        for (Iterator<D> i = dst.getChildren(dstPath);i.hasNext();) {
          D next = i.next();
          String name = dst.getName(next);
          P a = getChild(srcPath, name);

          //
          boolean remove;
          if (a == null) {
            remove = true;
          }
          else {
            switch (kind(a, dst, next)) {
              default:
                remove = true;
                break;
              case 0:
                remove = !filter.acceptFile(a, name);
                break;
              case 3:
                remove = !filter.acceptDir(a, name);
                break;
            }
          }

          // Remove or copy
          if (remove) {
            i.remove();
          }
          else {
            copy(a, filter, dst, next);
          }
        }

        //
        for (Iterator<P> i = getChildren(srcPath);i.hasNext();) {
          P next = i.next();
          String name = getName(next);
          D a = dst.getChild(dstPath, name);
          if (a == null) {
            boolean dir = isDir(next);
            boolean accept = dir ? filter.acceptDir(next, name) : filter.acceptFile(next, name);
            if (accept) {
              a = dir ? dst.addDir(dstPath, name) : dst.addFile(dstPath, name);
              copy(next, filter, dst, a);
            }
          }
          else {
            // We should not go in this case as the previous loop
            // took care of synchronizing everthing that was existing in the destination
          }
        }
        break;
      }
      default:
        throw new UnsupportedOperationException("Todo " + kind);
    }
  }

  private <D> int kind(P srcPath, ReadWriteFileSystem<D> dst, D dstPath) throws IOException {
    return (isDir(srcPath) ? 1 : 0) + (dst.isDir(dstPath) ? 2 : 0);
  }

  public final URL getURL() throws IOException {
    P root = getRoot();
    return getURL(root);
  }

  /** . */
  private final Charset encoding;

  protected ReadFileSystem() {
    // For now it's hardcoded
    this.encoding = Charset.defaultCharset();
  }

  public final Charset getEncoding() {
    return encoding;
  }

  public abstract boolean equals(P left, P right);

  public abstract String getName(P path) throws IOException;

  public abstract P getRoot() throws IOException;

  public abstract P getParent(P path) throws IOException;

  public abstract Iterator<P> getChildren(P dir) throws IOException;

  public abstract P getChild(P dir, String name) throws IOException;

  public abstract boolean isDir(P path) throws IOException;

  public abstract boolean isFile(P path) throws IOException;

  public abstract long getLastModified(P path) throws IOException;

}
