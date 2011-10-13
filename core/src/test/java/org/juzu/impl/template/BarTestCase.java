package org.juzu.impl.template;

import junit.framework.TestCase;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.CompilerHelper;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BarTestCase extends TestCase
{

   public void testResolution() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "template", "url", "resolution");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
   }

   public void testInvalidMethodName() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "template", "url", "invalid_method_name");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      CompilationError error = compiler.failCompile().get(0);
      assertEquals("/template/url/invalid_method_name/A.java", error.getSource());
   }

   public void testInvalidMethodArgs() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "template", "url", "invalid_method_args");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      CompilationError error = compiler.failCompile().get(0);
      assertEquals("/template/url/invalid_method_args/A.java", error.getSource());
   }
}
