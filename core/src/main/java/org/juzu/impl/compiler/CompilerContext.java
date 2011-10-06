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
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerContext<I, O>
{

   /** . */
   final List<URL> classPath;

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
      this(Collections.<URL>emptyList(), input, output);
   }

   public CompilerContext(List<URL> classPath, ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this.classPath = classPath;
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

      // Build classpath
      List<String> options = new ArrayList<String>();
      if (classPath.size() > 0)
      {
         StringBuilder sb = new StringBuilder();
         for (URL url : classPath)
         {
            sb.append(url.getFile()).append(File.pathSeparator);
         }
         options.add("-classpath");
         options.add(sb.toString());
      }


      //
      final AtomicBoolean failed = new AtomicBoolean(false);
      DiagnosticListener<JavaFileObject> listener = new DiagnosticListener<JavaFileObject>()
      {
         public void report(Diagnostic<? extends JavaFileObject> diagnostic)
         {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR)
            {
               failed.set(true);
            }
         }
      };

      //
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, sources);
      task.setProcessors(processors);

      // We don't use the return value because sometime it says it is failed although
      // it is not, need to investigate this at some piont
      task.call();

      //
      return !failed.get();
   }
}
