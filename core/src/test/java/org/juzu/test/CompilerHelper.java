package org.juzu.test;

import org.juzu.impl.application.JuzuProcessor;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerHelper<S>
{

   /** . */
   private ReadFileSystem<S> in;

   /** . */
   private RAMFileSystem out;

   /** . */
   private ClassLoader cl;

   public CompilerHelper(ReadFileSystem<S> in)
   {
      try
      {
         this.in = in;
         this.out = new RAMFileSystem();
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public List<CompilationError> failCompile()
   {
      try
      {
         Compiler<S, RAMPath> compiler = new org.juzu.impl.compiler.Compiler<S, RAMPath>(in, out);
         compiler.addAnnotationProcessor(new JuzuProcessor());
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertTrue("Was expecting compilation to fail", errors.size() > 0);
         return errors;
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public void assertCompile()
   {
      try
      {
         Compiler<S, RAMPath> compiler = new org.juzu.impl.compiler.Compiler<S, RAMPath>(in, out);
         compiler.addAnnotationProcessor(new JuzuProcessor());
         AbstractTestCase.assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
         cl = new URLClassLoader(new URL[]{out.getURL()}, Thread.currentThread().getContextClassLoader());
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public Class<?> assertClass(String className)
   {
      try
      {
         return cl.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }
}
