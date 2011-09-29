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
public class FileKey
{

   public static FileKey newResourceName(String className) throws IOException
   {
      int pos = className.lastIndexOf('.');
      if (pos == -1)
      {
         return new FileKey("", className, JavaFileObject.Kind.OTHER);
      }
      else
      {
         return new FileKey(className.substring(0, pos), className.substring(pos + 1), JavaFileObject.Kind.OTHER);
      }
   }

   public static FileKey newResourceName(String packageName, String name) throws IOException
   {
      return new FileKey(packageName, name, JavaFileObject.Kind.OTHER);
   }

   public static FileKey newJavaName(String className, JavaFileObject.Kind kind) throws IOException
   {
      if (kind == JavaFileObject.Kind.SOURCE || kind == JavaFileObject.Kind.CLASS)
      {
         int pos = className.lastIndexOf('.');
         if (pos == -1)
         {
            return new FileKey("", className, kind);
         }
         else
         {
            return new FileKey(className.substring(0, pos), className.substring(pos + 1), kind);
         }
      }
      else
      {
         throw new IllegalArgumentException("Kind " + kind + " not accepted");
      }
   }

   public static FileKey newJavaName(String packageName, String name) throws IOException
   {
      JavaFileObject.Kind kind;
      if (name.endsWith(".java"))
      {
         kind = JavaFileObject.Kind.SOURCE;
      }
      else if (name.endsWith(".class"))
      {
         kind = JavaFileObject.Kind.CLASS;
      }
      else
      {
         throw new IllegalArgumentException("Illegal name " + name);
      }
      String rawName = name.substring(0, name.length() - kind.extension.length());
      return new FileKey(packageName, rawName, kind);
   }

   /** . */
   final String packageName;

   /** . */
   final String rawName;

   /** . */
   final String fqn;

   /** . */
   final URI uri;

   /** . */
   final JavaFileObject.Kind kind;

   private FileKey(String packageName, String rawName, JavaFileObject.Kind kind) throws IOException
   {
      String path;
      String fqn;
      if (packageName.length() == 0)
      {
         path = "/" + rawName + kind.extension;
         fqn = rawName;
      }
      else
      {
         path = "/" + packageName.replace('.', '/') + '/' + rawName + kind.extension;
         fqn = packageName + "." + rawName;
      }
      try
      {
         this.packageName = packageName;
         this.rawName = rawName;
         this.uri = new URI(path);
         this.fqn = fqn;
         this.kind = kind;
      }
      catch (URISyntaxException e)
      {
         throw new IOException("Could not create path " + path, e);
      }
   }

   public FileKey as(JavaFileObject.Kind kind) throws IOException
   {
      return new FileKey(packageName, rawName, kind);
   }

   @Override
   public final int hashCode()
   {
      return packageName.hashCode() ^ rawName.hashCode() ^ kind.hashCode();
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
         return packageName.equals(that.packageName) && rawName.equals(that.rawName) && kind.equals(that.kind);
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "FileKey[packageName=" + packageName + ",rawName=" + rawName + ",kind=" + kind + "]";
   }
}
