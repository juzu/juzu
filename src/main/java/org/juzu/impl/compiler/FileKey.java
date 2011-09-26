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

package org.juzu.impl.compiler;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class FileKey
{

   public static FileKey newClassKey(String className, JavaFileObject.Kind kind) throws IOException
   {
      return new FileKey("/" + className.replace('.', '/'), kind);
   }

   /** . */
   final String rawPath;

   /** . */
   final URI uri;

   /** . */
   final JavaFileObject.Kind kind;

   FileKey(String rawPath, JavaFileObject.Kind kind) throws IOException
   {
      String path = rawPath + kind.extension;
      try
      {
         this.rawPath = rawPath;
         this.uri = new URI(path);
         this.kind = kind;
      }
      catch (URISyntaxException e)
      {
         throw new IOException("Could not create path " + rawPath, e);
      }
   }

   @Override
   public final int hashCode()
   {
      return uri.hashCode();
   }

   @Override
   public final boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      else if (obj instanceof FileKey)
      {
         FileKey that = (FileKey)obj;
         return uri.equals(that.uri);
      }
      else
      {
         return false;
      }
   }
}
