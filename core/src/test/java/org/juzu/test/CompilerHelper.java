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

package org.juzu.test;

import org.juzu.Application;
import org.juzu.impl.processor.MainProcessor;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.jar.JarFileSystem;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.request.MockApplication;

import javax.annotation.processing.Processor;
import javax.enterprise.context.spi.Context;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerHelper<I, O>
{

   public static <I> CompilerHelper<I, RAMPath> create(ReadFileSystem<I> input)
   {
      try
      {
         return new CompilerHelper<I, RAMPath>(input, new RAMFileSystem());
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   /** . */
   private ReadFileSystem<I> input;

   /** . */
   private ReadWriteFileSystem<O> sourceOutput;

   /** . */
   private ReadWriteFileSystem<O> classOutput;

   /** . */
   private ClassLoader classLoader;

   /** . */
   private Processor annotationProcessor;

   public CompilerHelper(
      ReadFileSystem<I> input,
      ReadWriteFileSystem<O> sourceOutput,
      ReadWriteFileSystem<O> classOutput)
   {
      this.input = input;
      this.sourceOutput = sourceOutput;
      this.classOutput = classOutput;
      this.annotationProcessor = new MainProcessor();
   }

   public CompilerHelper(ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this(input, output, output);
   }

   public Processor getAnnotationProcessor()
   {
      return annotationProcessor;
   }

   public void setAnnotationProcessor(Processor annotationProcessor)
   {
      this.annotationProcessor = annotationProcessor;
   }

   public ReadWriteFileSystem<O> getClassOutput()
   {
      return classOutput;
   }

   public ReadWriteFileSystem<O> getSourceOutput()
   {
      return sourceOutput;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   private Compiler buildCompiler() throws Exception
   {
      ArrayList<ReadFileSystem<?>> classPath = new ArrayList<ReadFileSystem<?>>();

      //
      URL url1 = Application.class.getProtectionDomain().getCodeSource().getLocation();
      classPath.add(new DiskFileSystem(new File(url1.toURI())));
      URL url2 = CompilerHelper.class.getProtectionDomain().getCodeSource().getLocation();
      classPath.add(new DiskFileSystem(new File(url2.toURI())));

      //
      classPath.add(new JarFileSystem(new JarFile(new File(Inject.class.getProtectionDomain().getCodeSource().getLocation().toURI()))));
      classPath.add(new JarFileSystem(new JarFile(new File(Context.class.getProtectionDomain().getCodeSource().getLocation().toURI()))));

      //
      Compiler compiler = new Compiler(input, classPath, sourceOutput, classOutput);
      compiler.addAnnotationProcessor(annotationProcessor);
      return compiler;
   }

   public List<CompilationError> failCompile()
   {
      try
      {
         Compiler compiler = buildCompiler();
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertTrue("Was expecting compilation to fail", errors.size() > 0);
         return errors;
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public MockApplication<?> application(InjectBootstrap bootstrap)
   {
      try
      {
         ClassLoader classLoader = new URLClassLoader(new URL[]{getClassOutput().getURL()}, Thread.currentThread().getContextClassLoader());
         return new MockApplication<O>(getClassOutput(), classLoader, bootstrap);
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public Compiler assertCompile()
   {
      try
      {
         Compiler compiler = buildCompiler();
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertEquals(Collections.<CompilationError>emptyList(), errors);
         classLoader = new URLClassLoader(new URL[]{classOutput.getURL()}, Thread.currentThread().getContextClassLoader());
         return compiler;
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public Class<?> assertClass(String className)
   {
      try
      {
         return classLoader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }
}
