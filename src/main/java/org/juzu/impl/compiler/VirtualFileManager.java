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

import org.juzu.impl.spi.fs.FileSystem;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class VirtualFileManager<P, D extends P, F extends P> extends ForwardingJavaFileManager<StandardJavaFileManager>
{

   /** . */
   final FileSystem<P, D, F> fs;

   /** . */
   final Map<FileKey, VirtualJavaFileObject.Class> files;

   /** . */
   final Map<FileKey, VirtualJavaFileObject.GeneratedResource> resources;

   /** . */
   final LinkedList<VirtualJavaFileObject.CompiledClass> modifications;

   public VirtualFileManager( FileSystem<P, D, F> fs, StandardJavaFileManager fileManager)
   {
      super(fileManager);

      //
      this.fs = fs;
      this.files = new HashMap<FileKey, VirtualJavaFileObject.Class>();
      this.modifications = new LinkedList<VirtualJavaFileObject.CompiledClass>();
      this.resources = new HashMap<FileKey, VirtualJavaFileObject.GeneratedResource>();
   }


   public Collection<VirtualJavaFileObject.FileSystem<P, D, F>> collectJavaFiles() throws IOException
   {
      D root = fs.getRoot();
      ArrayList<VirtualJavaFileObject.FileSystem<P, D, F>> javaFiles = new ArrayList<VirtualJavaFileObject.FileSystem<P, D, F>>();
      collectJavaFiles(root, javaFiles);
      return javaFiles;
   }

   private void collectJavaFiles(D dir, ArrayList<VirtualJavaFileObject.FileSystem<P, D, F>> javaFiles) throws IOException
   {
      for (Iterator<P> i = fs.getChildren(dir);i.hasNext();)
      {
         P child = i.next();
         if (fs.isFile(child))
         {
            String name = fs.getName(child);
            if (name.endsWith(".java"))
            {
               F javaFile = fs.asFile(child);
               FileKey key = getURI(javaFile);
               javaFiles.add(new VirtualJavaFileObject.FileSystem<P, D, F>(fs, javaFile, key));
            }
         }
         else
         {
            D childDir = fs.asDir(child);
            collectJavaFiles(childDir, javaFiles);
         }
      }
   }

   private FileKey getURI(String pkgName, String name) throws IOException
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
      else if (name.endsWith(".html"))
      {
         kind = JavaFileObject.Kind.HTML;
      }
      else
      {
         kind = JavaFileObject.Kind.OTHER;
      }
      String rawName = name.substring(0, name.length() - kind.extension.length());
      String rawPath;
      if (pkgName.length() == 0)
      {
         rawPath = "/" + rawName;
      }
      else
      {
         rawPath = "/" + pkgName.replace('.', '/') + '/' + rawName;
      }
      return new FileKey(rawPath, kind);
   }

   private FileKey getURI(F file) throws IOException
   {
      StringBuilder pkgName = foo(fs.getParent(file));
      return getURI(pkgName.toString(), fs.getName(file));
   }

   private StringBuilder foo(P file) throws IOException
   {
      P parent = fs.getParent(file);
      if (parent == null)
      {
         return new StringBuilder("");
      }
      else if (fs.equals(parent, fs.getRoot()))
      {
         return new StringBuilder("").append(fs.getName(file));
      }
      else
      {
         return foo(parent).append('.').append(fs.getName(file));
      }
   }

   // **************

   @Override
   public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException
   {
      Iterable<JavaFileObject> s = super.list(location, packageName,  kinds, recurse);

      List<JavaFileObject> ret = Collections.emptyList();
      if (location == StandardLocation.CLASS_OUTPUT && kinds.contains(JavaFileObject.Kind.CLASS))
      {
         Pattern pattern = Tools.getPackageMatcher(packageName, recurse);
         Matcher matcher = null;
         for (VirtualJavaFileObject.Class file : files.values())
         {
            if (kinds.contains(file.key.kind))
            {
               if (matcher == null)
               {
                  matcher = pattern.matcher(file.className);
               }
               else
               {
                  matcher.reset(file.className);
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
      if (file instanceof VirtualJavaFileObject.Class)
      {
         VirtualJavaFileObject.Class fileClass = (VirtualJavaFileObject.Class)file;
         return fileClass.className;
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
         D current = fs.getRoot();
         Spliterator s = new Spliterator(packageName, '.');
         while (s.hasNext())
         {
            String name = s.next();
            P child = fs.getChild(current, name);
            if (child != null || fs.isDir(child))
            {
               current = fs.asDir(child);
            }
            else
            {
               current = null;
               break;
            }
         }
         if (current != null)
         {
            P child = fs.getChild(current, relativeName);
            if (child != null && fs.isFile(child))
            {
               F file = fs.asFile(child);
               return new VirtualJavaFileObject.FileSystem<P, D, F>(fs, file, getURI(file));
            }
         }
         throw new IllegalArgumentException("Could not locate pkg=" + packageName + " name=" + relativeName + ")");
      }
      else if (location == StandardLocation.CLASS_OUTPUT)
      {
         FileKey key = getURI(packageName, relativeName);
         VirtualJavaFileObject.GeneratedResource file = resources.get(key);
         if (file == null)
         {
            resources.put(key, file = new VirtualJavaFileObject.GeneratedResource(key));
         }
         return file;
      }
      else
      {
         return super.getFileForOutput(location, packageName, relativeName, sibling);
      }
   }

   @Override
   public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
   {
      if (kind == JavaFileObject.Kind.CLASS)
      {
         FileKey key = FileKey.newClassKey(className, JavaFileObject.Kind.CLASS);
         VirtualJavaFileObject.CompiledClass file = (VirtualJavaFileObject.CompiledClass)files.get(key);
         if (file == null)
         {
            files.put(key, file = new VirtualJavaFileObject.CompiledClass(this, className, key));
         }
         return file;
      }
      else if (kind == JavaFileObject.Kind.SOURCE)
      {
         // Generated by annotation processor
         FileKey key = FileKey.newClassKey(className, JavaFileObject.Kind.SOURCE);
         VirtualJavaFileObject.GeneratedSource file = (VirtualJavaFileObject.GeneratedSource)files.get(key);
         if (file == null)
         {
            files.put(key, file = new VirtualJavaFileObject.GeneratedSource(className, key));
         }
         return file;
      }
      else
      {
         throw new UnsupportedOperationException("Kind " + kind + " not supported");
      }
   }
}
