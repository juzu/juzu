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

package org.juzu.impl.spi.fs.disk;

import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DiskFileSystem extends ReadFileSystem<File>
{

   /** . */
   private final File root;

   /** . */
   private final FilenameFilter filter;

   public DiskFileSystem(File root)
   {
      this(root, new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            return true;
         }
      });
   }

   public DiskFileSystem(File root, FilenameFilter filter)
   {
      this.root = root;
      this.filter = filter;
   }

   public DiskFileSystem(final File root, final String rootName)
   {
      this(root, new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            return !(dir == root && !name.equals(rootName));
         }
      });
   }

   @Override
   public boolean equals(File left, File right)
   {
      return left.equals(right);
   }

   @Override
   public File getRoot() throws IOException
   {
      return root;
   }

   @Override
   public File getParent(File path) throws IOException
   {
      if (path.equals(root))
      {
         return null;
      }
      else
      {
         return path.getParentFile();
      }
   }

   @Override
   public boolean isDir(File path) throws IOException
   {
      return path.isDirectory();
   }

   @Override
   public boolean isFile(File path) throws IOException
   {
      return path.isFile();
   }

   @Override
   public String getName(File path) throws IOException
   {
      if (path.equals(root))
      {
         return "";
      }
      else
      {
         return path.getName();
      }
   }

   @Override
   public Iterator<File> getChildren(File dir) throws IOException
   {
      return Arrays.asList(dir.listFiles(filter)).iterator();
   }

   @Override
   public File getChild(File dir, String name) throws IOException
   {
      if (filter.accept(dir, name))
      {
         File child = new File(dir, name);
         if (child.exists())
         {
            return child;
         }
      }
      return null;
   }

   @Override
   public Content getContent(File file) throws IOException
   {
      FileInputStream in = new FileInputStream(file);
      try
      {
         ByteArrayOutputStream content = new ByteArrayOutputStream();
         byte[] buffer = new byte[256];
         for (int l = in.read(buffer);l != -1;l = in.read(buffer))
         {
            content.write(buffer, 0, l);
         }
         return new Content.ByteArray(file.lastModified(), content.toByteArray());
      }
      finally
      {
         Tools.safeClose(in);
      }
   }

   @Override
   public long getLastModified(File path) throws IOException
   {
      return path.lastModified();
   }

   @Override
   public URL getURL(File path) throws IOException
   {
      return path.toURI().toURL();
   }

   @Override
   public File getFile(File path) throws IOException
   {
      return path;
   }
}
