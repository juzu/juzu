package org.juzu.impl.template;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.CompilerHelper;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BarTestCase extends TestCase
{

   public void testNoArg() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "template_url");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
//      compiler.assertClass("controller1.A");

      //
//      a_Class = compiler.assertClass("controller1.A_");
   }
}
