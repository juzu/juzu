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

import junit.framework.AssertionFailedError;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.utils.Content;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationTestCase extends AbstractTestCase
{

   public void testBar() throws Exception
   {
      Compiler<?, ?> compiler = compiler("compiler", "disk").assertCompile();
      assertEquals(2, compiler.getClassOutput().size(ReadFileSystem.FILE));
   }

   public void testGetResourceFromProcessor() throws Exception
   {
      DiskFileSystem input = diskFS("compiler", "getresource");

      //
      @javax.annotation.processing.SupportedAnnotationTypes({"*"})
      @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
      class ProcessorImpl extends AbstractProcessor
      {

         /** . */
         Object result = null;

         @Override
         public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
         {
            if (roundEnv.processingOver())
            {
               try
               {
                  Filer filer = processingEnv.getFiler();
                  FileObject o = filer.getResource(StandardLocation.SOURCE_PATH, "compiler.getresource", "A.txt");
                  result = o.getCharContent(false);
               }
               catch (IOException e)
               {
                  result = e;
               }
            }
            return false;
         }
      }

      //
      Compiler<File, RAMPath> compiler = new Compiler<File, RAMPath>(input, new RAMFileSystem());
      ProcessorImpl processor = new ProcessorImpl();
      compiler.addAnnotationProcessor(processor);
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
      assertEquals(1, compiler.getClassOutput().size(ReadFileSystem.FILE));
      if (processor.result instanceof Exception)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause((Throwable)processor.result);
         throw afe;
      }
      else if (processor.result instanceof CharSequence)
      {
         assertEquals("value", processor.result.toString());
      }
      else
      {
         fail("Was not expecting result to be " + processor.result);
      }
   }

   // For now we don't support this until we figure the feature fully
   public void _testChange() throws Exception
   {
      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMDir foo = root.addDir("foo");
      RAMFile a = foo.addFile("A.java").update("package foo; public class A {}");
      RAMFile b = foo.addFile("B.java").update("package foo; public class B {}");

      Compiler<RAMPath, ?> compiler = new Compiler<RAMPath, RAMPath>(ramFS, new RAMFileSystem());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
      assertEquals(2, compiler.getClassOutput().size(ReadFileSystem.FILE));
      Content aClass = compiler.getClassOutput().getContent("foo", "A");
      assertNotNull(aClass);
      Content bClass = compiler.getClassOutput().getContent("foo", "B");
      assertNotNull(bClass);

      //
      while (true)
      {
         b.update("package foo; public class B extends A {}");
         if (bClass.getLastModified() < b.getLastModified())
         {
            break;
         }
         else
         {
            Thread.sleep(1);
         }
      }

      //
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
      assertEquals(1, compiler.getClassOutput().size(ReadFileSystem.FILE));
      bClass = compiler.getClassOutput().getContent("foo", "B");
      assertNotNull(bClass);
   }

   @javax.annotation.processing.SupportedAnnotationTypes({"*"})
   @javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
   public static class ProcessorImpl extends AbstractProcessor
   {

      /** . */
      final List<String> names = new ArrayList<String>();

      /** . */
      private boolean done;

      @Override
      public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
      {
         for (Element elt : roundEnv.getRootElements())
         {
            if (elt instanceof TypeElement)
            {
               TypeElement typeElt = (TypeElement)elt;
               names.add(typeElt.getQualifiedName().toString());
            }
         }

         //
         if (!done)
         {
            try
            {
               Filer filer = processingEnv.getFiler();
               JavaFileObject b = filer.createSourceFile("compiler.processor.B");
               PrintWriter writer = new PrintWriter(b.openWriter());
               writer.println("package compiler.processor; public class B { }");
               writer.close();
               done = true;
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }

         //
         return true;
      }
   }

   public void testProcessor() throws Exception
   {
      DiskFileSystem ramFS = diskFS("compiler", "processor");
      Compiler<File, ?> compiler = new Compiler<File, RAMPath>(ramFS, new RAMFileSystem(), new RAMFileSystem());
      ProcessorImpl processor = new ProcessorImpl();
      compiler.addAnnotationProcessor(processor);
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
      assertEquals(2, compiler.getClassOutput().size(ReadFileSystem.FILE));
      assertEquals(Arrays.asList("compiler.processor.A", "compiler.processor.B"), processor.names);
      assertEquals(1, compiler.getSourceOutput().size(ReadFileSystem.FILE));
   }

   public void testCompilationFailure() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("compiler", "failure");
      assertEquals(1, compiler.failCompile().size());
   }

   public void testAnnotationException() throws Exception
   {
      DiskFileSystem fs = diskFS("compiler", "annotationexception");

      //
      Compiler<File, ?> compiler = new Compiler<File, RAMPath>(fs, new RAMFileSystem());
      ProcessorPlugin plugin = new ProcessorPlugin()
      {
         @Override
         public void process() throws CompilationException
         {
            Set<? extends Element> elements = getElementsAnnotatedWith(Deprecated.class);
            if (elements.size() == 1)
            {
               Element elt = elements.iterator().next();
               throw new CompilationException(elt, "the_message");
            }
         }
      };
      compiler.addAnnotationProcessor(new Processor(plugin));
      List<CompilationError> errors = compiler.compile();
      assertEquals(1, errors.size());
      CompilationError error = errors.get(0);
      assertEquals("/compiler/annotationexception/A.java", error.getSource());
      assertTrue(error.getMessage().contains("the_message"));
      assertNotNull(error.getSourceFile());
      assertTrue(error.getSourceFile().getAbsolutePath().endsWith("compiler/annotationexception/A.java"));
      assertNotNull(error.getLocation());

      //
      compiler = new Compiler<File, RAMPath>(fs, new RAMFileSystem());
      plugin = new ProcessorPlugin()
      {
         boolean failed = false;
         @Override
         public void process() throws CompilationException
         {
            if (!failed)
            {
               failed = true;
               throw new NoSuchElementException("the_message");
            }
         }
      };
      compiler.addAnnotationProcessor(new Processor(plugin));
      errors = compiler.compile();
      assertEquals(1, errors.size());
      error = errors.get(0);
      assertEquals(null, error.getSource());
      assertTrue(error.getMessage().contains("the_message"));
      assertNull(error.getSourceFile());
      assertNull(error.getLocation());
   }

   public void testIncremental() throws IOException
   {
      RAMFileSystem sourcePath = new RAMFileSystem();
      RAMFileSystem output = new RAMFileSystem();
      Compiler<RAMPath, RAMPath> compiler = new Compiler<RAMPath, RAMPath>(sourcePath, output);

      //
      RAMDir incremental = sourcePath.addDir(sourcePath.getRoot(), "compiler").addDir("incremental");
      RAMFile a = incremental.addFile("A.java").update("package compiler.incremental; public class A {}");
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile("compiler/incremental/A.java"));
      assertEquals(1, compiler.getClassOutput().size(ReadFileSystem.FILE));
      a.remove();

      //
      RAMFile b = incremental.addFile("B.java").update("package compiler.incremental; public class B extends A {}");
      compiler = new Compiler<RAMPath, RAMPath>(sourcePath, output, output, output);
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile("compiler/incremental/B.java"));
   }
}
