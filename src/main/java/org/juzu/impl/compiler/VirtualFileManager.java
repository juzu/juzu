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

import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.fs.Visitor;
import org.juzu.impl.utils.Spliterator;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class VirtualFileManager<I, O> extends ForwardingJavaFileManager<StandardJavaFileManager>
{

   /** . */
   final ReadFileSystem<I> input;

   /** . */
   final Map<FileKey, VirtualJavaFileObject.RandomAccess> classOutput;

   /** . */
   final Map<FileKey, VirtualJavaFileObject.RandomAccess> sourceOutput;

   /** . */
   final ReadWriteFileSystem<O> output;

   public VirtualFileManager(ReadFileSystem<I> input, StandardJavaFileManager fileManager, ReadWriteFileSystem<O> output)
   {
      super(fileManager);

      //
      this.input = input;
      this.classOutput = new HashMap<FileKey, VirtualJavaFileObject.RandomAccess>();
      this.sourceOutput = new HashMap<FileKey, VirtualJavaFileObject.RandomAccess>();
      this.output = output;
   }

   public Collection<VirtualJavaFileObject.FileSystem<I>> collectJavaFiles() throws IOException
   {
      final ArrayList<VirtualJavaFileObject.FileSystem<I>> javaFiles = new ArrayList<VirtualJavaFileObject.FileSystem<I>>();
      input.traverse(new Visitor.Default<I>()
      {
         public void file(I file, String name) throws IOException
         {
            if (name.endsWith(".java"))
            {
               FileKey key = FileKey.newJavaName(input.packageName(file).toString(), name);
               javaFiles.add(new VirtualJavaFileObject.FileSystem<I>(input, file, key));
            }
         }
      });
      return javaFiles;
   }

   private Map<FileKey, VirtualJavaFileObject.RandomAccess> getFiles(Location location)
   {
      if (location instanceof StandardLocation)
      {
         switch ((StandardLocation)location)
         {
            case SOURCE_OUTPUT:
               return sourceOutput;
            case CLASS_OUTPUT:
               return classOutput;
         }
      }
      return null;
   }

   // **************

   @Override
   public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException
   {
      Iterable<JavaFileObject> s = super.list(location, packageName,  kinds, recurse);
      List<JavaFileObject> ret = Collections.emptyList();

      //
      Map<FileKey, VirtualJavaFileObject.RandomAccess> files = getFiles(location);
      if (files != null)
      {
         Pattern pattern = Tools.getPackageMatcher(packageName, recurse);
         Matcher matcher = null;
         for (VirtualJavaFileObject file : files.values())
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
                  if (ret.isEmpty())
                  {
                     ret = new ArrayList<JavaFileObject>();
                  }
                  ret.add(file);
               }
            }
         }
      }

      //
      if (ret.isEmpty())
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
      if (file instanceof VirtualJavaFileObject.RandomAccess)
      {
         VirtualJavaFileObject.RandomAccess fileClass = (VirtualJavaFileObject.RandomAccess)file;
         return fileClass.key.fqn;
      }
      else
      {
         return super.inferBinaryName(location, file);
      }
   }

   @Override
   public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException
   {
      if (location == StandardLocation.SOURCE_PATH)
      {
         I current = input.getRoot();
         Spliterator s = new Spliterator(packageName, '.');
         while (s.hasNext())
         {
            String name = s.next();
            I child = input.getChild(current, name);
            if (child != null || input.isDir(child))
            {
               current = child;
            }
            else
            {
               current = null;
               break;
            }
         }
         if (current != null)
         {
            I child = input.getChild(current, relativeName);
            if (child != null && input.isFile(child))
            {
               FileKey uri = FileKey.newResourceName(input.packageName(child).toString(), input.getName(child));
               return new VirtualJavaFileObject.FileSystem<I>(input, child, uri);
            }
         }
         throw new IllegalArgumentException("Could not locate pkg=" + packageName + " name=" + relativeName + ")");
      }
      else
      {
         Map<FileKey, VirtualJavaFileObject.RandomAccess> files = getFiles(location);
         if (files != null)
         {
            FileKey key = FileKey.newResourceName(packageName, relativeName);
            VirtualJavaFileObject.RandomAccess file = files.get(key);
            if (file == null)
            {
               files.put(key, file = new VirtualJavaFileObject.RandomAccess.Text(this, key));
            }
            return file;
         }
         else
         {
            return super.getFileForOutput(location, packageName, relativeName, sibling);
         }
      }
   }

   @Override
   public boolean isSameFile(FileObject a, FileObject b)
   {
      VirtualJavaFileObject va = (VirtualJavaFileObject)a;
      VirtualJavaFileObject vb = (VirtualJavaFileObject)b;
      return va.key.equals(vb.key);
   }

   @Override
   public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
   {
      if (location == StandardLocation.CLASS_OUTPUT && kind == JavaFileObject.Kind.CLASS)
      {
         FileKey key = FileKey.newJavaName(className, JavaFileObject.Kind.CLASS);
         VirtualJavaFileObject.RandomAccess.Binary file = (VirtualJavaFileObject.RandomAccess.Binary)classOutput.get(key);
         if (file == null)
         {
            classOutput.put(key, file = new VirtualJavaFileObject.RandomAccess.Binary(this, key));
         }
         return file;
      }
      else if (location == StandardLocation.SOURCE_OUTPUT && kind == JavaFileObject.Kind.SOURCE)
      {
         // Generated by annotation processor
         FileKey key = FileKey.newJavaName(className, JavaFileObject.Kind.SOURCE);
         VirtualJavaFileObject.RandomAccess.Text file = (VirtualJavaFileObject.RandomAccess.Text)sourceOutput.get(key);
         if (file == null)
         {
            sourceOutput.put(key, file = new VirtualJavaFileObject.RandomAccess.Text(this, key));
         }
         return file;
      }
      else
      {
         throw new UnsupportedOperationException("Kind " + kind + " not supported with location " + location);
      }
   }
}
