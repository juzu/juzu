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

package juzu.impl.fs.spi.url;

import juzu.impl.common.Content;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLFileSystem extends ReadFileSystem<Node> {

  /** . */
  private final Node root;

  public URLFileSystem() throws IOException {
    this.root = new Node();
  }

  /**
   * Add the resources from the specified classloader only.
   *
   * @param loader the loader
   * @return this url file system
   * @throws IOException any io exception
   * @throws URISyntaxException any uri syntax exception
   */
  public URLFileSystem add(ClassLoader loader) throws IOException, URISyntaxException {
    return add(loader, loader.getParent());
  }

  /**
   * Add the resources from the <code>from</code> classloader up to the <code>to</code> classloader
   * that is excluded.
   *
   * @param from the classloader from which resources are included
   * @param to the classloader from which resources are excluded
   * @return this url file system
   * @throws IOException any io exception
   * @throws URISyntaxException any uri syntax exception
   */
  public URLFileSystem add(ClassLoader from, ClassLoader to) throws IOException, URISyntaxException {

    // Get urls from loader
    HashSet<URL> urls = Tools.set(from.getResources(""));
    for (Enumeration<URL> e = from.getResources("META-INF/MANIFEST.MF"); e.hasMoreElements();) {
      URL url = e.nextElement();
      if ("jar".equals(url.getProtocol())) {
        urls.add(url);
      }
    }

    // Remove URLs from extension classloader and above (bootstrap)
    if (to != null) {
      for (Enumeration<URL> e = to.getResources("");e.hasMoreElements();) {
        urls.remove(e.nextElement());
      }
      for (Enumeration<URL> e = to.getResources("META-INF/MANIFEST.MF"); e.hasMoreElements();) {
        URL url = e.nextElement();
        if ("jar".equals(url.getProtocol())) {
          urls.remove(url);
        }
      }
    }

    // Now handle urls
    for (URL url : urls) {
      add(url);
    }

    // Add manually this one (fucked up jar: no META-INF/MANIFEST.MF)
    if (Inject.class.getClassLoader() == from) {
      add(from.getResource(Inject.class.getName().replace('.', '/') + ".class"));
    }

    //
    return this;
  }

  public URLFileSystem add(URL url) throws IOException, URISyntaxException {
    String protocol = url.getProtocol();
    if ("file".equals(protocol)) {
      File file = new File(url.toURI());
      if (file.isDirectory()) {
        root.merge(file);
      } else {
        JarFile jar = new JarFile(file, false);
        for (JarEntry entry : Tools.iterable(jar.entries())) {
          root.merge(url, entry);
        }
      }
    } else if ("jar".equals(protocol)) {
      String path = url.getPath();
      int pos = path.lastIndexOf("!/");
      if (pos == -1) {
        throw new MalformedURLException("Malformed URL " + url);
      }
      URL inner = new URL(path.substring(0, pos));
      add(inner);
    } else {
      throw new UnsupportedOperationException("Cannot handle url " + url + " yet");
    }
    return this;
  }

  @Override
  public Class<Node> getType() {
    return null;
  }

  @Override
  public String getDescription() {
    return "URLFileSystem[]";
  }

  @Override
  public boolean equals(Node left, Node right) {
    return left == right;
  }

  @Override
  public Node getRoot() throws IOException {
    return root;
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
