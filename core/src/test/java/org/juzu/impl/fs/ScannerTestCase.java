package org.juzu.impl.fs;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;

import java.io.IOException;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScannerTestCase extends TestCase
{

   public void testFoo() throws IOException
   {
      RAMFileSystem fs = new RAMFileSystem();
      FileSystemScanner<RAMPath> scanner = new FileSystemScanner<RAMPath>(fs);

      //
      assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

      //
      RAMPath foo = fs.addDir(fs.getRoot(), "foo");
      assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

      //
      RAMFile bar = fs.addFile(foo, "bar.txt");
      assertEquals(Collections.singletonMap("foo/bar.txt", Change.ADD), scanner.scan());
      assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

      //
      bar.update("value");
      assertEquals(Collections.singletonMap("foo/bar.txt", Change.UPDATE), scanner.scan());
      assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

      //
      bar.remove();
      assertEquals(Collections.singletonMap("foo/bar.txt", Change.REMOVE), scanner.scan());
      assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());
   }

}
