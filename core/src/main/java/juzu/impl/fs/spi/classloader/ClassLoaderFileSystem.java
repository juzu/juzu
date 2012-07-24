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

package juzu.impl.fs.spi.classloader;

import juzu.impl.fs.spi.SimpleFileSystem;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;
import juzu.impl.common.Trie;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClassLoaderFileSystem extends SimpleFileSystem<Trie<String, URL>> {

  /** . */
  private final URLCache cache;

  /** . */
  private final ClassLoader classLoader;

  public ClassLoaderFileSystem(ClassLoader classLoader) throws IOException {
    // For now we do that here
    URLCache cache = new URLCache();
    cache.add(classLoader);
    cache.add(Inject.class);

    //
    this.cache = cache;
    this.classLoader = classLoader;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public String getDescription() {
    return "ClassLoader[]";
  }

  @Override
  public Trie<String, URL> getPath(Iterable<String> names) throws IOException {
    return cache.get(names);
  }

  @Override
  public String getName(Trie<String, URL> path) throws IOException {
    return path.getKey();
  }

  @Override
  public void packageOf(Trie<String, URL> path, Collection<String> to) throws IOException {
    Trie<String, URL> trie = cache.get(path.getPath());
    if (trie == null) {
      throw new IOException();
    }
    Iterator<String> iterator = path.getPath().iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      if (trie.value() == null || iterator.hasNext()) {
        to.add(next);
      }
    }
  }

  @Override
  public Iterator<Trie<String, URL>> getChildren(Trie<String, URL> dir) throws IOException {
    final Iterator<Trie<String, URL>> entries = dir.getEntries();
    return new Iterator<Trie<String, URL>>() {

      /** . */
      private Trie<String, URL> next;

      public boolean hasNext() {
        while (next == null && entries.hasNext()) {
          Trie<String, URL> next = entries.next();
          if (next.value() != null) {
            this.next = next;
          }
        }
        return next != null;
      }

      public Trie<String, URL> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        try {
          return next;
        }
        finally {
          next = null;
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean isDir(Trie<String, URL> path) throws IOException {
    return path.value() == null;
  }

  @Override
  public boolean isFile(Trie<String, URL> path) throws IOException {
    return !isDir(path);
  }

  @Override
  public Content getContent(Trie<String, URL> file) throws IOException {
    URL url = file.value();
    if (url == null) {
      throw new IOException("Cannot find file " + url);
    }

    //
    URLConnection conn = url.openConnection();
    long lastModified = conn.getLastModified();
    byte[] bytes = Tools.bytes(conn.getInputStream());
    return new Content(lastModified, bytes, Charset.defaultCharset());
  }

  @Override
  public File getFile(Trie<String, URL> path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public URL getURL(Trie<String, URL> path) throws NullPointerException, IOException {
    return path.value();
  }
}
