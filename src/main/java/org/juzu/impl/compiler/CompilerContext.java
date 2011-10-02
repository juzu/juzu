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
import org.juzu.impl.utils.Content;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerContext<I, O>
{

   /** . */
   final ReadFileSystem<I> input;

   /** . */
   private JavaCompiler compiler;

   /** . */
   private VirtualFileManager<I, O> fileManager;

   /** . */
   private Set<Processor> processors;

   public CompilerContext(ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this.input = input;
      this.compiler = ToolProvider.getSystemJavaCompiler();
      this.fileManager = new VirtualFileManager<I, O>(input, compiler.getStandardFileManager(null, null, null), output);
      this.processors = new HashSet<Processor>();
   }

   public void addAnnotationProcessor(Processor annotationProcessorType)
   {
      if (annotationProcessorType == null)
      {
         throw new NullPointerException("No null processor allowed");
      }
      processors.add(annotationProcessorType);
   }

   public Set<FileKey> getClassOutputKeys()
   {
      return fileManager.classOutput.keySet();
   }

   public Content<?> getClassOutput(FileKey key)
   {
      VirtualJavaFileObject.RandomAccess file = fileManager.classOutput.get(key);
      return file != null ? file.content : null;
   }

   public Set<FileKey> getSourceOutputKeys()
   {
      return fileManager.sourceOutput.keySet();
   }

   public Content<?> getSourceOutput(FileKey key)
   {
      VirtualJavaFileObject.RandomAccess file = fileManager.sourceOutput.get(key);
      return file != null ? file.content : null;
   }

   public boolean compile() throws IOException
   {
      Collection<VirtualJavaFileObject.FileSystem<I>> sources = fileManager.collectJavaFiles();

      //
      fileManager.classOutput.clear();
      fileManager.sourceOutput.clear();

      // Filter compiled files
      for (Iterator<VirtualJavaFileObject.FileSystem<I>> i = sources.iterator();i.hasNext();)
      {
         VirtualJavaFileObject.FileSystem<I> source = i.next();
         FileKey key = source.key;
         VirtualJavaFileObject.RandomAccess.Binary existing = (VirtualJavaFileObject.RandomAccess.Binary)fileManager.classOutput.get(key.as(JavaFileObject.Kind.CLASS));
         // For now we don't support this feature
/*
         if (existing != null)
         {
            ClassFile cf = existing.getFile();
            if (cf != null && cf.getLastModified() >= source.getLastModified())
            {
               i.remove();
            }
         }
*/
      }

      //
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, sources);
      task.setProcessors(processors);

      //
      return task.call();
   }
}
