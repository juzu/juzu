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

import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.Spliterator;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FileSystemURLStreamHandler<P> extends URLStreamHandler
{

   /** . */
   private ReadFileSystem<P> fs;

   public FileSystemURLStreamHandler(ReadFileSystem<P> fs)
   {
      this.fs = fs;
   }

   @Override
   protected URLConnection openConnection(URL u) throws IOException
   {
      List<String> names = Spliterator.split(u.getPath().substring(1), '/');
      P path = fs.getPath(names);
      if (path != null && fs.isFile(path))
      {
         Content<?> content = fs.getContent(path);
         if (content != null)
         {
            return new FileSystemURLConnection(u, content);
         }
      }
      throw new IOException("Could not connect to non existing content " + names);
   }
}
