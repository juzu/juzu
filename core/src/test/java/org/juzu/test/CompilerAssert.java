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
import org.juzu.impl.metamodel.MetaModelProcessor;
import org.juzu.impl.spi.fs.classloader.ClassLoaderFileSystem;
import org.juzu.impl.utils.Tools;
import org.juzu.processor.MainProcessor;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.protocol.mock.MockApplication;

import javax.annotation.processing.Processor;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Make compile assertion.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CompilerAssert<I, O>
{

   public static final Provider<MetaModelProcessor> META_MODEL_PROCESSOR_FACTORY = new Provider<MetaModelProcessor>()
   {
      public MetaModelProcessor get()
      {
         return new MainProcessor();
      }
   };

   /** A cache to speed up unit tests. */
   private static WeakHashMap<ClassLoader, ClassLoaderFileSystem> classPathCache = new WeakHashMap<ClassLoader, ClassLoaderFileSystem>();

   /** . */
   private ClassLoader baseClassLoader;

   /** . */
   private ClassLoader classLoader;

   /** . */
   private CompileStrategy<I, O> strategy;

   public CompilerAssert(
      boolean incremental,
      ReadWriteFileSystem<I> sourcePath,
      ReadWriteFileSystem<O> sourceOutput,
      ReadWriteFileSystem<O> classOutput)
   {
      ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();

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
      this.strategy = incremental ? new CompileStrategy.Incremental<I, O>(
         classPath,
         sourcePath,
         sourceOutput,
         classOutput,
         META_MODEL_PROCESSOR_FACTORY) : new CompileStrategy.Global<I, O>(
         classPath,
         sourcePath,
         sourceOutput,
         classOutput,
         META_MODEL_PROCESSOR_FACTORY);

      //
      this.baseClassLoader = baseClassLoader;
   }

   public CompilerAssert(
      ReadWriteFileSystem<I> sourcePath,
      ReadWriteFileSystem<O> sourceOutput,
      ReadWriteFileSystem<O> classOutput)
   {
      this(false, sourcePath, sourceOutput, classOutput);
   }

   public CompilerAssert(ReadWriteFileSystem<I> sourcePath, ReadWriteFileSystem<O> output)
   {
      this(false, sourcePath, output, output);
   }

   public CompilerAssert(boolean incremental, ReadWriteFileSystem<I> sourcePath, ReadWriteFileSystem<O> output)
   {
      this(incremental, sourcePath, output, output);
   }

   public CompilerAssert<I, O> with(Provider<? extends Processor> processorFactory)
   {
      strategy.processorFactory = processorFactory;
      return this;
   }

   public CompilerAssert<I, O> addClassPath(ReadFileSystem<?> classPath)
   {
      strategy.addClassPath(classPath);
      return this;
   }

   public ReadFileSystem<I> getSourcePath()
   {
      return strategy.sourcePath;
   }

   public ReadWriteFileSystem<O> getClassOutput()
   {
      return strategy.classOutput;
   }

   public ReadWriteFileSystem<O> getSourceOutput()
   {
      return strategy.sourceOutput;
   }

   public CompilerAssert<I, O> formalErrorReporting(boolean formalErrorReporting)
   {
      if (formalErrorReporting)
      {
         strategy.config.withProcessorOption("juzu.error_reporting", "formal");
      }
      else
      {
         strategy.config.withProcessorOption("juzu.error_reporting", null);
      }
      return this;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public List<CompilationError> failCompile()
   {
      try
      {
         List<CompilationError> errors = strategy.compile();
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
         List<CompilationError> errors = strategy.compile();
         AbstractTestCase.assertEquals("Compilation failed : " + errors, Collections.<CompilationError>emptyList(), errors);
         classLoader = new URLClassLoader(new URL[]{strategy.classOutput.getURL()}, baseClassLoader);
         return strategy.compiler;
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
   
   public void assertRemove(String ... names)
   {
      try
      {
         I path = strategy.sourcePath.getPath(names);
         if (path == null)
         {
            throw AbstractTestCase.failure("Cannot remove path " + Tools.join('/', names));
         }
         strategy.sourcePath.removePath(path);
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public JavaFile<I> assertJavaFile(String... names)
   {
      I path;
      try
      {
         path = strategy.sourcePath.getPath(names);
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
      if (path == null)
      {
         throw AbstractTestCase.failure("Was not expecting " + Arrays.asList(names) + " to be null file");
      }
      return new JavaFile<I>(strategy.sourcePath, path);
   }
}
