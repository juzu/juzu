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

package juzu.impl.fs.spi.war;

import juzu.impl.common.Resource;
import juzu.impl.common.Spliterator;
import juzu.impl.common.Timestamped;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Tools;

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WarFileSystem extends ReadFileSystem<String> {

  /** . */
  private final String mountPoint;

  public WarFileSystem(String mountPoint) throws NullPointerException {
    if (mountPoint == null) {
      throw new NullPointerException("No null mount point accepted");
    }
    if (!mountPoint.startsWith("/") || !mountPoint.endsWith("/")) {
      throw new IllegalArgumentException("Invalid mount point " + mountPoint);
    }

    //
    this.mountPoint = mountPoint.substring(0, mountPoint.length() - 1);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

  @Override
  public boolean equals(String left, String right) {
    return left.equals(right);
  }

  @Override
  public String getRoot() {
    return "/";
  }

  @Override
  public String getName(String path) {
    // It's a directory, remove the trailing '/'
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    // Get index of last '/'
    int index = path.lastIndexOf('/');

    // Return name
    return path.substring(index + 1);
  }

  @Override
  public Iterable<String> getNames(String path) {
    int to = path.length();
    if (path.endsWith("/")) {
      to--;
    }
    return Spliterator.split(path, 0, to, '/');
  }

  @Override
  public Iterator<String> getChildren(String dir) throws IOException {
    return getResourcePaths(dir).iterator();
  }

  @Override
  public String getChild(String dir, String name) throws IOException {
    for (Iterator<String> i = getChildren(dir);i.hasNext();) {
      String child = i.next();
      String childName = getName(child);
      if (childName.equals(name)) {
        return child;
      }
    }
    return null;
  }

  @Override
  public PathType typeOf(String path) throws IOException {
    if (path.endsWith("/")) {
      return PathType.DIR;
    } else {
      URL url = getResourceURL(path);
      if (url != null) {
        return PathType.FILE;
      } else {
        return null;
      }
    }
  }

  @Override
  public Timestamped<Resource> getResource(String file) throws IOException {
    URL url = getResourceURL(file);
    if (url != null) {
      URLConnection conn = url.openConnection();
      long lastModified = conn.getLastModified();
      InputStream in = conn.getInputStream();
      try {
        ByteArrayOutputStream resource = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
          resource.write(buffer, 0, l);
        }
        return new Timestamped<Resource>(lastModified, new Resource(resource.toByteArray(), Charset.defaultCharset()));
      }
      finally {
        Tools.safeClose(in);
      }
    }
    else {
      throw new UnsupportedOperationException("handle me gracefully");
    }
  }

  @Override
  public long getLastModified(String path) throws IOException {
    URL url = getResourceURL(path);
    URLConnection conn = url.openConnection();
    return conn.getLastModified();
  }

  @Override
  public URL getURL(String path) throws IOException {
    return getResourceURL(path);
  }

  protected abstract Set<String> doGetResourcePaths(String path) throws IOException;

  protected abstract URL doGetResource(String path) throws IOException;

  protected abstract String doGetRealPath(String path);

  private Collection<String> getResourcePaths(String path) throws IOException {
    Set<String> resourcePaths = doGetResourcePaths(mountPoint + path);
    if (resourcePaths != null) {
      ArrayList<String> tmp = new ArrayList<String>(resourcePaths.size());
      for (String resourcePath : resourcePaths) {
        tmp.add(resourcePath.substring(mountPoint.length()));
      }
      return tmp;
    }
    else {
      return Collections.emptyList();
    }
  }

  @Override
  public File getFile(String path) {
    String realPath = doGetRealPath(mountPoint + path);
    return realPath == null ? null : new File(realPath);
  }

  private URL getResourceURL(String path) throws IOException {
    return doGetResource(mountPoint + path);
  }

  public static WarFileSystem create(ServletContext context) {
    return create(context, "/");
  }

  public static WarFileSystem create(final ServletContext servletContext, String mountPoint) {
    return new WarFileSystem(mountPoint) {
      @Override
      public String getDescription() {
        return "servlet[" + servletContext.getRealPath("/") + "]";
      }

      @Override
      protected Set<String> doGetResourcePaths(String path) throws IOException {
        return servletContext.getResourcePaths(path);
      }

      @Override
      protected URL doGetResource(String path) throws IOException {
        String realPath = servletContext.getRealPath(path);
        if (realPath != null) {
          return new File(realPath).toURI().toURL();
        }
        else {
          return servletContext.getResource(path);
        }
      }

      @Override
      protected String doGetRealPath(String path) {
        return servletContext.getRealPath(path);
      }
    };
  }

  public static WarFileSystem create(PortletContext portletContext) {
    return create(portletContext, "/");
  }

  public static WarFileSystem create(final PortletContext portletContext, String mountPoint) {
    return new WarFileSystem(mountPoint) {
      @Override
      public String getDescription() {
        return "servlet[" + portletContext.getRealPath("/") + "]";
      }

      @Override
      protected Set<String> doGetResourcePaths(String path) throws IOException {
        return portletContext.getResourcePaths(path);
      }

      @Override
      protected URL doGetResource(String path) throws IOException {
        String realPath = portletContext.getRealPath(path);
        if (realPath != null) {
          return new File(realPath).toURI().toURL();
        }
        else {
          return portletContext.getResource(path);
        }
      }

      @Override
      protected String doGetRealPath(String path) {
        return portletContext.getRealPath(path);
      }
    };
  }
}
