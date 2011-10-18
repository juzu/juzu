package org.juzu.impl.request;

import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;
import org.juzu.test.Registry;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleTestCase extends AbstractTestCase
{

   public void testRenderPhase() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "request", "lifecycle");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      ClassLoader cl2 = new URLClassLoader(new URL[]{compiler.getOutput().getURL()}, Thread.currentThread().getContextClassLoader());

      //
      MockApplication<RAMPath> app = new MockApplication<RAMPath>(compiler.getOutput(), cl2);
      app.init();

      //
      MockClient client = app.client();
      Registry.clear();
      client.render();
      Integer count = Registry.get("count");
      assertEquals((Integer)2, count);
   }
}
