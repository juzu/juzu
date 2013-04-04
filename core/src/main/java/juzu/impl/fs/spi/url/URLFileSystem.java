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

  public URLFileSystem() {
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

    // Get file urls from loader
    HashSet<URL> urls = Tools.set(from.getResources(""));

    // Get jar urls from loader
    for (Enumeration<URL> e = from.getResources("META-INF/MANIFEST.MF"); e.hasMoreElements();) {
      URL url = e.nextElement();
      if ("jar".equals(url.getProtocol())) {
        urls.add(url);
      }
    }

    // Remove URLs from extension classloader and above (bootstrap)
    if (to != null) {

      // Remove file urls we don't need
      for (Enumeration<URL> e = to.getResources("");e.hasMoreElements();) {
        urls.remove(e.nextElement());
      }

      // Remove jar urls we don't need
      for (Enumeration<URL> e = to.getResources("META-INF/MANIFEST.MF"); e.hasMoreElements();) {
        URL url = e.nextElement();
        if ("jar".equals(url.getProtocol())) {
          urls.remove(url);
        }
      }
    } else {

    }

    // Add manually this one (fucked up jar: no META-INF/MANIFEST.MF)
    ClassLoader injectCL = Inject.class.getClassLoader();
    URL injectURL = from.getResource(Inject.class.getName().replace('.', '/') + ".class");
    if (injectURL != null) {
      if (to != null) {
        for (ClassLoader current = from;current != to;current = current.getParent()) {
          if (current == injectCL) {
            urls.add(injectURL);
            break;
          }
        }
      } else {
        urls.add(injectURL);
      }
    }

    // Now handle urls
    for (URL url : urls) {
      if (url.getProtocol().equals("jar")) {
        // Correct URL to get root and not manifest
        String s = url.toString();
        int pos = s.lastIndexOf("!/");
        add(new URL(s.substring(0, pos + 2)));
      } else {
        add(url);
      }
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
          root.merge("jar:" + url + "!/", entry.getName());
        }
      }
    } else if ("jar".equals(protocol)) {
      String path = url.getPath();
      int pos = path.lastIndexOf("!/");
      if (pos == -1) {
        throw new MalformedURLException("Malformed URL " + url);
      }
      URL inner = new URL(path.substring(0, pos));
      if (inner.getProtocol().equals("file")) {
        File file = new File(inner.toURI());
        if (file.isDirectory()) {
          throw new IllegalArgumentException("Wrong jar URL " + url);
        } else {
          String prefix = path.substring(pos + 2);
          if (prefix.length() > 0 && !prefix.endsWith("/")) {
            throw new IllegalArgumentException("Wrong nested jar URL, should end with a / or be empty" + url);
          }
          JarFile jar = new JarFile(file, false);
          for (JarEntry entry : Tools.iterable(jar.entries())) {
            String name = entry.getName();
            if (name.startsWith(prefix)) {
              root.merge("jar:" + inner + "!/" + prefix, name.substring(prefix.length()));
            }
          }
        }
      } else {
        throw new UnsupportedOperationException("Not yet supported");
      }
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
  public String getName(Node path) {
    return path.getKey();
  }

  @Override
  public Iterator<Node> getChildren(Node dir) throws IOException {
    return dir.getEntries();
/*
    return new Iterator<Node>() {

      */
/** . *//*

      private Node next;

      public boolean hasNext() {
        while (next == null && entries.hasNext()) {
          Node next = entries.next();
          this.next = next;
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
*/
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
