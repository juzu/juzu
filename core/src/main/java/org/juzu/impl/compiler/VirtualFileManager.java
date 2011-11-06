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

import org.juzu.impl.compiler.file.FileKey;
import org.juzu.impl.compiler.file.FileManager;
import org.juzu.impl.compiler.file.JavaFileObjectImpl;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class VirtualFileManager extends ForwardingJavaFileManager<JavaFileManager>
{

   /** . */
   final FileManager<?> sourcePath;

   /** . */
   final FileManager<?> classOutput;

   /** . */
   final FileManager<?> classPath;

   /** . */
   final FileManager<?> sourceOutput;

   public VirtualFileManager(
      JavaFileManager fileManager,
      ReadFileSystem<?> sourcePath,
      ReadFileSystem<?> classPath,
      ReadWriteFileSystem<?> sourceOutput,
      ReadWriteFileSystem<?> classOutput)
   {
      super(fileManager);

      //
      this.sourcePath = safeWrap(sourcePath);
      this.classPath = safeWrap(classPath);
      this.classOutput = safeWrap(classOutput);
      this.sourceOutput = safeWrap(sourceOutput);
   }

   private <P> FileManager<P> safeWrap(ReadFileSystem<P> fs)
   {
      return fs != null ? new FileManager<P>(fs) : null;
   }

   private FileManager<?> getFiles(Location location)
   {
      if (location instanceof StandardLocation)
      {
         switch ((StandardLocation)location)
         {
            case SOURCE_PATH:
               return sourcePath;
            case SOURCE_OUTPUT:
               return sourceOutput;
            case CLASS_OUTPUT:
               return classOutput;
            case CLASS_PATH:
               return classPath;
         }
      }
      return null;
   }

   // **************

   @Override
   public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException
   {
      Iterable<JavaFileObject> s = super.list(location, packageName, kinds, recurse);
      List<JavaFileObject> ret = null;
      FileManager<?> files = getFiles(location);
      if (files != null)
      {
         files.list(packageName, kinds, recurse, ret = new ArrayList<JavaFileObject>());
      }

      //
      if (ret == null)
      {
         return s;
      }
      else
      {
         for (JavaFileObject o : s)
         {
            ret.add(o);
         }
         return ret;
      }
   }

   @Override
   public String inferBinaryName(Location location, JavaFileObject file)
   {
      if (file instanceof JavaFileObjectImpl<?>)
      {
         JavaFileObjectImpl<?> fileClass = (JavaFileObjectImpl<?>)file;
         return fileClass.getKey().fqn;
      }
      else
      {
         return super.inferBinaryName(location, file);
      }
   }

   @Override
   public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException
   {
      FileKey key = FileKey.newResourceName(packageName, relativeName);
      FileManager<?> files = getFiles(location);
      if (files != null)
      {
         return files.getReadable(key);
      }
      else
      {
         throw new FileNotFoundException("Cannot write: " + location);
      }
   }

   @Override
   public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException
   {
      FileKey key = FileKey.newResourceName(packageName, relativeName);

      // Address a bug
      if (location == StandardLocation.SOURCE_PATH)
      {
         FileObject file = sourcePath.getReadable(key);
         if (file == null)
         {
            throw new FileNotFoundException("Not found:" + key.toString());
         }
         return file;
      }
      else
      {
         FileManager<?> files = getFiles(location);
         if (files != null)
         {
            return files.getWritable(key);
         }
         else
         {
            throw new FileNotFoundException("Cannot write: " + location);
         }
      }
   }

   @Override
   public boolean isSameFile(FileObject a, FileObject b)
   {
      FileKey ka = ((JavaFileObjectImpl)a).getKey();
      FileKey kb = ((JavaFileObjectImpl)b).getKey();
      return ka.equals(kb);
   }

   @Override
   public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
   {
      FileManager<?> files = getFiles(location);
      if (files != null)
      {
         FileKey key = FileKey.newJavaName(className, kind);
         return files.getWritable(key);
      }
      else
      {
         throw new UnsupportedOperationException("Location " + location + " not supported");
      }
   }
}
