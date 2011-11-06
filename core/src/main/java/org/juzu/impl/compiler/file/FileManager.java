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

package org.juzu.impl.compiler.file;

import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FileManager<P>
{

   /** . */
   final ReadFileSystem<P> fs;

   /** . */
   final Map<FileKey, JavaFileObjectImpl<P>> entries;

   public FileManager(ReadFileSystem<P> fs)
   {
      this.fs = fs;
      this.entries = new HashMap<FileKey, JavaFileObjectImpl<P>>();
   }

   public ReadFileSystem<P> getFileSystem()
   {
      return fs;
   }

   public void clearCache()
   {
      entries.clear();
   }

   public JavaFileObject getReadable(FileKey key) throws IOException
   {
      JavaFileObjectImpl<P> entry = entries.get(key);
      if (entry == null)
      {
         P dir = fs.getDir(key.packageNames);
         if (dir != null)
         {
            P file = fs.getChild(dir, key.name);
            if (file != null)
            {
               entries.put(key, entry = new JavaFileObjectImpl(key, this, file));
            }
         }
      }
      return entry;
   }

   public JavaFileObject getWritable(FileKey key) throws IOException
   {
      if (fs instanceof ReadWriteFileSystem<?>)
      {
         ReadWriteFileSystem<P> rwFS = (ReadWriteFileSystem<P>)fs;
         JavaFileObjectImpl<P> entry = entries.get(key);
         if (entry == null)
         {
            P file = rwFS.makeFile(key.packageNames, key.name);
            entries.put(key, entry = new JavaFileObjectImpl<P>(key, this, file));
         }
         return entry;
      }
      else
      {
         throw new UnsupportedOperationException("File system is not writable");
      }
   }

   public void list(
      String packageName,
      Set<JavaFileObject.Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> to) throws IOException
   {
      Pattern pattern = org.juzu.impl.utils.Tools.getPackageMatcher(packageName, recurse);
      Matcher matcher = null;
      for (JavaFileObjectImpl<P> file : entries.values())
      {
         if (kinds.contains(file.key.kind))
         {
            if (matcher == null)
            {
               matcher = pattern.matcher(file.key.packageFQN);
            }
            else
            {
               matcher.reset(file.key.packageFQN);
            }
            if (matcher.matches())
            {
               to.add(file);
            }
         }
      }
   }
}
