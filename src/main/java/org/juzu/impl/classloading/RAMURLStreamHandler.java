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

package org.juzu.impl.classloading;

import org.juzu.impl.utils.Content;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMURLStreamHandler extends URLStreamHandler
{

   /** . */
   private Map<String, Content<?>> contentMap;

   public RAMURLStreamHandler(Map<String, Content<?>> contentMap)
   {
      this.contentMap = contentMap;
   }

   @Override
   protected URLConnection openConnection(URL u) throws IOException
   {
      String path = u.getPath();
      Content<?> content = contentMap.get(path);
      if (content == null)
      {
         throw new IOException("Could not connect to non existing content " + path);
      }
      return new RAMURLConnection(u, content);
   }
}
