/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <p></p>The <code>DevClassLoader</code> blacklists any class from found in the <code>/WEB-INF/classes</code> folder and
 * instead throws a {@link ClassNotFoundException} to the caller, forcing the caller to load the class by itself.</p>
 *
 * <p>At the moment it only supports unpacked war files.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DevClassLoader extends ClassLoader
{

   public DevClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   @Override
   protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      Class<?> found = super.loadClass(name, resolve);

      //
      if (found.getClassLoader() == super.getParent())
      {
         String classPath = name.replace('.', '/') + ".class";
         URL url = getResource(classPath);
         if (url == null)
         {
            throw new ClassNotFoundException();
         }
      }

      //
      return found;
   }

   @Override
   public URL getResource(String name)
   {
      URL url = super.getResource(name);

      //
      if (url != null && shouldLoad(url, name))
      {
         return url;
      }
      else
      {
         return null;
      }
   }

   private boolean shouldLoad(URL url, String name)
   {
      // Unwrap until we get the file location
      String protocol = url.getProtocol();
      if ("file".equals(protocol))
      {
         String path = url.getPath();
         if (path.endsWith("/WEB-INF/classes/" + name))
         {
            return false;
         }
         else
         {
            return true;
         }
      }
      else if ("jar".equals(protocol))
      {
         String path = url.getPath();
         int index = path.indexOf("!/");
         String nested = path.substring(0, index);
         if (nested.endsWith(".jar"))
         {
            return true;
         }
         else
         {
            throw new UnsupportedOperationException("handle me gracefully " + url);
         }
      }
      else
      {
         throw new UnsupportedOperationException("handle me gracefully " + url);
      }
   }

}
