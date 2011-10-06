package org.juzu.impl.spi.fs.ram;

import org.juzu.test.AbstractTestCase;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFileSystemTestCase extends AbstractTestCase
{

   public void testLastModified() throws IOException
   {
      RAMFileSystem fs = new RAMFileSystem();
      RAMFile fooTxt = fs.addFile(fs.getRoot(), "foo.txt").update("abc");
      long now = waitForOneMillis();
      assertTrue(fs.getLastModified(fooTxt) < now);
      waitForOneMillis();
      fooTxt.update("def");
      assertTrue(now < fs.getLastModified(fooTxt));
   }
}
