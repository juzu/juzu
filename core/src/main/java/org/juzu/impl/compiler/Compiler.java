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
import org.juzu.text.Location;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Compiler<I, O>
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

   public Compiler(ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this(Collections.<URL>emptyList(), input, output);
   }

   public Compiler(List<URL> classPath, ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this(classPath, input, output, output);
   }

   public Compiler(ReadFileSystem<I> input, ReadWriteFileSystem<O> sourceOutput, ReadWriteFileSystem<O> classOutput)
   {
      this(Collections.<URL>emptyList(), input, sourceOutput, classOutput);
   }

   public Compiler(List<URL> classPath, ReadFileSystem<I> input, ReadWriteFileSystem<O> sourceOutput, ReadWriteFileSystem<O> classOutput)
   {
      this.classPath = classPath;
      this.input = input;
      this.compiler = ToolProvider.getSystemJavaCompiler();
      this.fileManager = new VirtualFileManager<I, O>(input, compiler.getStandardFileManager(null, null, null), sourceOutput, classOutput);
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

   public ReadWriteFileSystem<O> getSourceOutput()
   {
      return fileManager.sourceOutput;
   }

   public ReadWriteFileSystem<O> getClassOutput()
   {
      return fileManager.classOutput;
   }

   public List<CompilationError> compile(String... compilationUnits) throws IOException
   {
      ArrayList<String> tmp = new ArrayList<String>();
      final ArrayList<VirtualJavaFileObject.FileSystem<I>> javaFiles = new ArrayList<VirtualJavaFileObject.FileSystem<I>>();
      for (String compilationUnit : compilationUnits)
      {
         ArrayList<String> names = Spliterator.split(compilationUnit, '/', tmp);
         String name = tmp.get(tmp.size() - 1);
         if (!name.endsWith(".java"))
         {
            throw new IllegalArgumentException("Illegal compilation unit: " + compilationUnit);
         }
         I file = input.getPath(names);
         if (file == null)
         {
            throw new IllegalArgumentException("Could not find compilation unit: " + compilationUnit);
         }
         StringBuilder sb = new StringBuilder();
         input.packageOf(file, '.', sb);
         FileKey key = FileKey.newJavaName(sb.toString(), name.substring(0, name.length()));
         javaFiles.add(new VirtualJavaFileObject.FileSystem<I>(input, file, key));
      }

      return compile(javaFiles);
   }

   public List<CompilationError> compile() throws IOException
   {

      final ArrayList<VirtualJavaFileObject.FileSystem<I>> javaFiles = new ArrayList<VirtualJavaFileObject.FileSystem<I>>();
      input.traverse(new Visitor.Default<I>()
      {
         public void file(I file, String name) throws IOException
         {
            if (name.endsWith(".java"))
            {
               StringBuilder sb = new StringBuilder();
               input.packageOf(file, '.', sb);
               FileKey key = FileKey.newJavaName(sb.toString(), name);
               javaFiles.add(new VirtualJavaFileObject.FileSystem<I>(input, file, key));
            }
         }
      });

      return compile(javaFiles);
   }

   private List<CompilationError> compile(Iterable<VirtualJavaFileObject.FileSystem<I>> compilationUnits) throws IOException
   {
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
      final List<CompilationError> errors = new ArrayList<CompilationError>();
      DiagnosticListener<JavaFileObject> listener = new DiagnosticListener<JavaFileObject>()
      {
         public void report(Diagnostic<? extends JavaFileObject> diagnostic)
         {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR)
            {
               int columnNumber = (int)diagnostic.getColumnNumber();
               int lineNumber = (int)diagnostic.getLineNumber();
               Location location = (columnNumber > 0 && lineNumber > 0) ? new Location(columnNumber, lineNumber) : null;
               String message = diagnostic.getMessage(null);

               //
               JavaFileObject obj = diagnostic.getSource();
               String source = null;
               File resolvedFile = null;
               if (obj != null)
               {
                  source = obj.getName().toString();
                  if (obj instanceof VirtualJavaFileObject.FileSystem)
                  {
                     VirtualJavaFileObject.FileSystem foo = (VirtualJavaFileObject.FileSystem)obj;
                     try
                     {
                        resolvedFile = foo.getFile();
                     }
                     catch (Exception e)
                     {
                     }

                  }
               }

               //
               errors.add(new CompilationError(source, resolvedFile, location, message));
            }
         }
      };

      //
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, compilationUnits);
      task.setProcessors(processors);

      // We don't use the return value because sometime it says it is failed although
      // it is not, need to investigate this at some piont
      task.call();

      //
      fileManager.openedClassOutput.clear();
      fileManager.openedSourceOutput.clear();

      //
      return errors;
   }
}
