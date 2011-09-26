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

package org.juzu.impl.spi.fs.ram;

import org.juzu.impl.spi.fs.Content;
import org.juzu.impl.spi.fs.FileSystem;

import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFileSystem implements FileSystem<RAMPath, RAMDir, RAMFile>
{

   /** . */
   private final RAMDir root;

   public RAMFileSystem()
   {
      this.root = new RAMDir();
   }

   public boolean equals(RAMPath left, RAMPath right)
   {
      return left == right;
   }

   public RAMDir getRoot() throws IOException
   {
      return root;
   }

   public RAMDir getParent(RAMPath path) throws IOException
   {
      return path.getParent();
   }

   public String getName(RAMPath path) throws IOException
   {
      return path.getName();
   }

   public Iterator<RAMPath> getChildren(RAMDir dir) throws IOException
   {
      return dir.children.values().iterator();
   }

   public boolean isDir(RAMPath path) throws IOException
   {
      return path instanceof RAMDir;
   }

   public boolean isFile(RAMPath path) throws IOException
   {
      return path instanceof RAMFile;
   }

   public RAMFile asFile(RAMPath path) throws IllegalArgumentException, IOException
   {
      return (RAMFile)path;
   }

   public RAMDir asDir(RAMPath path) throws IllegalArgumentException, IOException
   {
      return (RAMDir)path;
   }

   public Content getContent(RAMFile file) throws IOException
   {
      return file.getContent();
   }

   public long getLastModified(RAMPath path) throws IOException
   {
      return path.getLastModified();
   }
}
