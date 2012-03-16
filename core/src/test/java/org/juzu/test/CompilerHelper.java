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

import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.classloader.ClassLoaderFileSystem;
import org.juzu.processor.MainProcessor;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.protocol.mock.MockApplication;

import javax.annotation.processing.Processor;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerHelper<I, O>
{

   /** A cache to speed up unit tests. */
   private static WeakHashMap<ClassLoader, ClassLoaderFileSystem> classPathCache = new WeakHashMap<ClassLoader, ClassLoaderFileSystem>();

   /** . */
   private ReadFileSystem<I> sourcePath;

   /** . */
   private ReadWriteFileSystem<O> sourceOutput;

   /** . */
   private ReadWriteFileSystem<O> classOutput;

   /** . */
   private ClassLoader baseClassLoader;

   /** . */
   private ClassLoader classLoader;

   /** . */
   private Compiler.Builder builder;

   public CompilerHelper(
      ReadFileSystem<I> sourcePath,
      ReadWriteFileSystem<O> sourceOutput,
      ReadWriteFileSystem<O> classOutput)
   {
      this.sourcePath = sourcePath;
      this.sourceOutput = sourceOutput;
      this.classOutput = classOutput;
      this.baseClassLoader = Thread.currentThread().getContextClassLoader();
      
      //
      ClassLoaderFileSystem classPath = classPathCache.get(baseClassLoader);
      if (classPath == null)
      {
         try
         {
            classPathCache.put(baseClassLoader, classPath = new ClassLoaderFileSystem(baseClassLoader));
         }
         catch (IOException e)
         {
            throw AbstractTestCase.failure(e);
         }
      }

      //
      Compiler.Builder builder;
      try
      {
         builder = Compiler.builder();
         builder.addClassPath(classPath);
         builder.sourcePath(sourcePath);
         builder.sourceOutput(sourceOutput);
         builder.classOutput(classOutput);
         builder.processor(new MainProcessor());
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }

      //
      this.builder = builder;
   }

   public CompilerHelper(ReadFileSystem<I> sourcePath, ReadWriteFileSystem<O> output)
   {
      this(sourcePath, output, output);
   }

   public CompilerHelper<I, O> with(Processor annotationProcessor)
   {
      builder.processor(annotationProcessor);
      return this;
   }

   public CompilerHelper<I, O> addClassPath(ReadFileSystem<?> classPath)
   {
      builder.addClassPath(classPath);
      return this;
   }

   public ReadFileSystem<I> getSourcePath()
   {
      return sourcePath;
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

   public Compiler.Builder getBuilder()
   {
      return builder;
   }

   public List<CompilationError> failCompile()
   {
      try
      {
         Compiler compiler = builder.build();
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertTrue("Was expecting compilation to fail", errors.size() > 0);
         return errors;
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public MockApplication<?> application(InjectImplementation injectImplementation)
   {
      try
      {
         return new MockApplication<O>(getClassOutput(), classLoader, injectImplementation.bootstrap());
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
         Compiler compiler = builder.build();
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertEquals(Collections.<CompilationError>emptyList(), errors);
         classLoader = new URLClassLoader(new URL[]{classOutput.getURL()}, baseClassLoader);
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
