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

package juzu.impl.common;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <p>A classloader implementation that blacklists any class or resources not loaded from a jar file of the parent loader.</p
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ParentJarClassLoader extends ClassLoader {

  /** . */
  private final ClassLoader parent;

  public ParentJarClassLoader(ClassLoader parent) {
    super(parent);

    //
    this.parent = parent;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> found = super.loadClass(name, resolve);
    if (found.getClassLoader() == super.getParent()) {
      URL url = super.getResource(name.replace('.', '/') + ".class");
      if (shouldLoad(url)) {
        return found;
      } else {
        throw new ClassNotFoundException();
      }
    }
    return found;
  }

  static final java.lang.reflect.Method findResource;
  static final java.lang.reflect.Method findResources;
  static {
    try {
      findResource = ClassLoader.class.getDeclaredMethod("findResource", String.class);
      findResource.setAccessible(true);
      findResources = ClassLoader.class.getDeclaredMethod("findResources", String.class);
      findResources.setAccessible(true);
    }
    catch (NoSuchMethodException e) {
      throw new UnsupportedOperationException("Impossible to get findResource from ClassLoader class", e);
    }
  }

  @Override
  public URL getResource(String name) {
    URL url;
    try {
      url = (URL)findResource.invoke(parent, name);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("Not yet handled", e);
    }
    if (url != null) {
      if (shouldLoad(url)) {
        return url;
      } else {
        return null;
      }
    } else {
      return super.getResource(name);
    }
  }

  @Override
  public Enumeration<URL> getResources(final String name) throws IOException {
    Enumeration<URL> parentResources;
    try {
      parentResources = (Enumeration<URL>)findResources.invoke(parent, name);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("Not yet handled", e);
    }
    Enumeration<URL> ancestorResources = parent.getParent().getResources(name);
    final Enumeration<URL>[] resources = new Enumeration[]{parentResources, ancestorResources};
    return new Enumeration<URL>() {
      URL next = null;
      int index = 0;
      public boolean hasMoreElements() {
        while (next == null) {
          if (index < resources.length) {
            if (resources[index].hasMoreElements()) {
              URL url = resources[index].nextElement();
              if (index > 0 || shouldLoad(url)) {
                next = url;
              }
            } else {
              index++;
            }
          } else {
            break;
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

  private boolean shouldLoad(URL url) {
    String s = url.toString();
    return s.contains(".jar!/") || s.contains(".zip!/");
  }
}
