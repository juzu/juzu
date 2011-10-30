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

package org.juzu.impl.spi.fs;

import org.juzu.impl.utils.Content;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * File system provider interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ReadFileSystem<P>
{
   
   public final void dump(Appendable appendable) throws IOException
   {
      dump(getRoot(), appendable);
   }

   public final void dump(P path, final Appendable appendable) throws IOException
   {
      final StringBuilder prefix = new StringBuilder();
      traverse(path, new Visitor<P>()
      {
         public boolean enterDir(P dir, String name) throws IOException
         {
            prefix.append(name).append('/');
            return true;
         }
         public void file(P file, String name) throws IOException
         {
            appendable.append(prefix).append(name).append("\n");
         }
         public void leaveDir(P dir, String name) throws IOException
         {
            prefix.setLength(prefix.length() - 1 - name.length());
         }
      });
   }

   public final P getFile(Iterable<String> path, String name) throws IOException
   {
      P dir = getDir(path);
      if (dir != null)
      {
         P child = getChild(dir, name);
         if (isFile(child))
         {
            return child;
         }
      }
      return null;
   }

   public final P getDir(Iterable<String> path) throws IOException
   {
      P current = getRoot();
      for (String name : path)
      {
         P child = getChild(current, name);
         if (child != null && isDir(child))
         {
            current = child;
         }
         else
         {
            return null;
         }
      }
      return current;
   }

   public final void pathOf(P path, char separator, Appendable appendable) throws IOException
   {
      if (packageOf(path, separator, appendable))
      {
         appendable.append(separator);
      }
      String name = getName(path);
      appendable.append(name);
   }

   public final boolean packageOf(P path, char separator, Appendable appendable) throws NullPointerException, IOException
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }
      if (appendable == null)
      {
         throw new NullPointerException("No null appendable accepted");
      }
      if (isDir(path))
      {
         P parent = getParent(path);
         if (parent == null)
         {
            return false;
         }
         else
         {
            String name = getName(path);
            if (packageOf(parent, separator, appendable))
            {
               appendable.append(separator);
            }
            appendable.append(name);
            return true;
         }
      }
      else
      {
         return packageOf(getParent(path), separator, appendable);
      }
   }

   public final P getPath(Iterable<String> filePath) throws IOException
   {
      P current = getRoot();
      for (String name : filePath)
      {
         if (isDir(current))
         {
            P child = getChild(current, name);
            if (child != null)
            {
               current = child;
            }
            else
            {
               return null;
            }
         }
         else
         {
            throw new UnsupportedOperationException("handle me gracefully");
         }
      }
      return current;
   }

   public final void traverse(P path, Visitor<P> visitor) throws IOException
   {
      String name = getName(path);
      if (isDir(path))
      {
         if (visitor.enterDir(path, name))
         {
            for (Iterator<P> i = getChildren(path);i.hasNext();)
            {
               P child = i.next();
               traverse(child, visitor);
            }
            visitor.leaveDir(path, name);
         }
      }
      else
      {
         visitor.file(path, name);
      }
   }

   public final void traverse(Visitor<P> visitor) throws IOException
   {
      traverse(getRoot(), visitor);
   }

   public final URL getURL() throws IOException
   {
      P root = getRoot();
      return getURL(root);
   }

   /** . */
   private final Charset encoding;

   protected ReadFileSystem()
   {
      // For now it's hardcoded
      this.encoding = Charset.defaultCharset();
   }

   public final Charset getEncoding()
   {
      return encoding;
   }

   public abstract boolean equals(P left, P right);

   public abstract P getRoot() throws IOException;

   public abstract P getParent(P path) throws IOException;

   public abstract String getName(P path) throws IOException;

   public abstract Iterator<P> getChildren(P dir) throws IOException;

   public abstract P getChild(P dir, String name) throws IOException;

   public abstract boolean isDir(P path) throws IOException;

   public abstract boolean isFile(P path) throws IOException;

   public abstract Content getContent(P file) throws IOException;

   public abstract long getLastModified(P path) throws IOException;

   public abstract URL getURL(P path) throws IOException;

   /**
    * Attempt to return a {@link java.io.File} associated with this file or null if no physical file exists.
    *
    * @param path the path
    * @return the file system object
    * @throws IOException
    */
   public abstract File getFile(P path) throws IOException;

}
