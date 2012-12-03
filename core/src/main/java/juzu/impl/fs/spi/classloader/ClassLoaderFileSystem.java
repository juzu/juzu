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

import juzu.impl.common.Timestamped;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClassLoaderFileSystem extends ReadFileSystem<Node> {

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
  public Class<Node> getType() {
    return null;
  }

  @Override
  public String getDescription() {
    return "ClassLoader[]";
  }

  @Override
  public boolean equals(Node left, Node right) {
    return left == right;
  }

  @Override
  public Node getRoot() throws IOException {
    return cache.root;
  }

  @Override
  public Node getChild(Node dir, String name) throws IOException {
    return dir.get(name);
  }

  @Override
  public long getLastModified(Node path) throws IOException {
    return 1;
  }

  @Override
  public String getName(Node path) throws IOException {
    return path.getKey();
  }

  @Override
  public Iterator<Node> getChildren(Node dir) throws IOException {
    final Iterator<Node> entries = dir.getEntries();
    return new Iterator<Node>() {

      /** . */
      private Node next;

      public boolean hasNext() {
        while (next == null && entries.hasNext()) {
          Node next = entries.next();
          if (next.url != null) {
            this.next = next;
          }
        }
        return next != null;
      }

      public Node next() {
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
  public PathType typeOf(Node path) throws IOException {
    return path.url == null ? PathType.DIR : PathType.FILE;
  }

  @Override
  public Timestamped<Content> getContent(Node file) throws IOException {
    if (file.url == null) {
      throw new IOException("Cannot find file " + file.getPath());
    }

    //
    URLConnection conn = file.url.openConnection();
    long lastModified = conn.getLastModified();
    byte[] bytes = Tools.bytes(conn.getInputStream());
    return new Timestamped<Content>(lastModified, new Content(bytes, Charset.defaultCharset()));
  }

  @Override
  public File getFile(Node path){
    return null;
  }

  @Override
  public URL getURL(Node path) throws NullPointerException, IOException {
    return path.url;
  }
}
