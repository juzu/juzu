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

package juzu.impl.common;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <p></p>The <code>DevClassLoader</code> blacklists any class from found in the <code>/WEB-INF/classes</code> folder
 * and instead throws a {@link ClassNotFoundException} to the caller, forcing the caller to load the class by
 * itself.</p> <p/> <p>At the moment it only supports unpacked war files.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DevClassLoader extends ClassLoader {

  public DevClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> found = super.loadClass(name, resolve);

    //
    if (found.getClassLoader() == super.getParent()) {
      String classPath = name.replace('.', '/') + ".class";
      URL url = getResource(classPath);
      if (url == null) {
        throw new ClassNotFoundException();
      }
    }

    //
    return found;
  }

  @Override
  public URL getResource(String name) {
    URL url = super.getResource(name);

    //
    if (url != null && shouldLoad(url, name)) {
      return url;
    }
    else {
      try {
        Enumeration<URL> e = getResources(name);
        if (e.hasMoreElements()) {
          return e.nextElement();
        }
      }
      catch (IOException ignore) {
        //
      }
      return null;
    }
  }

  @Override
  public Enumeration<URL> getResources(final String name) throws IOException {
    final Enumeration<URL> a = super.getResources(name);
    return new Enumeration<URL>() {
      URL next = null;

      public boolean hasMoreElements() {
        while (next == null && a.hasMoreElements()) {
          URL url = a.nextElement();
          if (shouldLoad(url, name)) {
            next = url;
          }
        }
        return next != null;
      }

      public URL nextElement() {
        if (!hasMoreElements()) {
          throw new NoSuchElementException("No more elements");
        }
        try {
          return next;
        }
        finally {
          next = null;
        }
      }
    };
  }

  private boolean shouldLoad(URL url, String name) {
    // Unwrap until we get the file location
    String protocol = url.getProtocol();
    if ("file".equals(protocol)) {
      String path = url.getPath();
      if (path.endsWith("/WEB-INF/classes/" + name)) {
        return false;
      }
      else {
        return true;
      }
    }
    else if ("jar".equals(protocol)) {
      String path = url.getPath();
      int index = path.indexOf("!/");
      String nested = path.substring(0, index);
      if (nested.endsWith(".jar") || nested.endsWith(".zip")) {
        return true;
      }
      else {
        throw new UnsupportedOperationException("handle me gracefully " + url);
      }
    }
    else {
      throw new UnsupportedOperationException("handle me gracefully " + url);
    }
  }
}
