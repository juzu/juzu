package org.juzu.plugin.less;

import org.junit.Test;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.less.impl.LessMetaModelPlugin;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.CompilerAssert;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPluginTestCase extends AbstractInjectTestCase
{

   public LessPluginTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testCompile()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "compile");
      ca.assertCompile();
      File f = ca.getClassOutput().getPath("plugin", "less", "compile", "assets", "stylesheet.css");
      assertNotNull(f);
      assertTrue(f.exists());
   }

   @Test
   public void testFail()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "fail");
      List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
      assertEquals(1, errors.size());
      assertEquals(LessMetaModelPlugin.LESS_COMPILATION_ERROR, errors.get(0).getCode());
      File f = ca.getClassOutput().getPath("plugin", "less", "fail", "assets", "stylesheet.css");
      assertNull(f);
   }

   @Test
   public void testNotFound()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "notfound");
      List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
      assertEquals(1, errors.size());
      assertEquals(LessMetaModelPlugin.LESS_COMPILATION_ERROR, errors.get(0).getCode());
      File f = ca.getClassOutput().getPath("plugin", "less", "notfound", "assets", "stylesheet.css");
      assertNull(f);
   }

   @Test
   public void testMinify()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "minify");
      ca.assertCompile();
      File f = ca.getClassOutput().getPath("plugin", "less", "minify", "assets", "stylesheet.css");
      assertNotNull(f);
      assertTrue(f.exists());
      String s = Tools.read(f);
      assertFalse(s.contains(" "));
   }

   @Test
   public void testResolve()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "resolve");
      ca.assertCompile();
      File f = ca.getClassOutput().getPath("plugin", "less", "resolve", "assets", "stylesheet.css");
      assertNotNull(f);
      assertTrue(f.exists());
   }

   @Test
   public void testCannotResolve()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less", "cannotresolve");
      List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
      assertEquals(1, errors.size());
      assertEquals(LessMetaModelPlugin.LESS_COMPILATION_ERROR, errors.get(0).getCode());
      File f = ca.getClassOutput().getPath("plugin", "less", "cannotresolve", "assets", "stylesheet.css");
      assertNull(f);
   }
}
