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

package org.juzu.impl.fs;

import org.juzu.impl.spi.fs.ReadFileSystem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FileSystemScanner<P> implements Visitor<P>, Filter<P>
{

   /** . */
   private ReadFileSystem<P> fs;

   /** . */
   private StringBuilder sb = new StringBuilder();

   /** . */
   private Map<String, Data> snapshot;

   private static class Data
   {

      /** . */
      private long lastModified;

      /** . */
      private Change change;

      private Data(long lastModified)
      {
         this.lastModified = lastModified;
         this.change = Change.ADD;
      }
   }

   public ReadFileSystem<P> getFileSystem()
   {
      return fs;
   }

   public FileSystemScanner(ReadFileSystem<P> fs)
   {
      this.snapshot = new HashMap<String, Data>();
      this.fs = fs;
   }

   public Map<String, Change> scan() throws IOException
   {
      // Mark everything as removed
      for (Data data : snapshot.values())
      {
         data.change = Change.REMOVE;
      }

      // Update map
      fs.traverse(this, this);

      // Cleanup map and build change map
      Map<String, Change> changes = new LinkedHashMap<String, Change>();
      for (Iterator<Map.Entry<String, Data>> i = snapshot.entrySet().iterator();i.hasNext();)
      {
         Map.Entry<String, Data> entry = i.next();
         Data data = entry.getValue();
         if (data.change != null)
         {
            changes.put(entry.getKey(), data.change);
            if (data.change == Change.REMOVE)
            {
               i.remove();
            }
         }
      }

      //
      return changes;
   }

   public boolean acceptDir(P dir, String name) throws IOException
   {
      return !name.startsWith(".");
   }

   public boolean acceptFile(P file, String name) throws IOException
   {
      return !name.startsWith(".");
   }

   public void enterDir(P dir, String name) throws IOException
   {
   }

   public void file(P file, String name) throws IOException
   {
      long lastModified = fs.getLastModified(file);
      fs.pathOf(file, '/', sb);
      String id = sb.toString();
      sb.setLength(0);
      Data data = snapshot.get(id);
      if (data == null)
      {
         snapshot.put(id, new Data(lastModified));
      }
      else
      {
         if (data.lastModified < lastModified)
         {
            data.lastModified = lastModified;
            data.change = Change.UPDATE;
         }
         else
         {
            data.change = null;
         }
      }
   }

   public void leaveDir(P dir, String name) throws IOException
   {
   }
}
