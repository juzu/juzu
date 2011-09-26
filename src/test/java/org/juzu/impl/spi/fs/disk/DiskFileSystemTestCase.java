package org.juzu.impl.spi.fs.disk;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DiskFileSystemTestCase extends TestCase
{

   public void testFoo() throws Exception
   {
      File root = new File(System.getProperty("test.resources"));
      assertNotNull(root);
      assertNotNull(root.isDirectory());

      //
      doTest(new DiskFileSystem(root), root);
   }
   
   private <P, D extends P, F extends P> void doTest(FileSystem<P, D, F> fs, D root) throws IOException
   {
      
      assertEquals(root, fs.getRoot());

      //
      assertTrue(fs.isDir(root));
      assertFalse(fs.isFile(root));
      assertEquals("", fs.getName(root));
      assertEquals(null, fs.getParent(root));
      Iterator<P> rootChildren = fs.getChildren(root);
      assertTrue(rootChildren.hasNext());
      P org = rootChildren.next();
      assertFalse(rootChildren.hasNext());

      //
      assertTrue(fs.isDir(org));
      assertFalse(fs.isFile(org));
      assertEquals("org", fs.getName(org));
      assertEquals(root, fs.getParent(org));
      Iterator<P> orgChildren = fs.getChildren(fs.asDir(org));
      assertTrue(orgChildren.hasNext());
      P juzu = orgChildren.next();
      assertFalse(orgChildren.hasNext());

      //
      assertTrue(fs.isDir(juzu));
      assertFalse(fs.isFile(juzu));
      assertEquals("juzu", fs.getName(juzu));
      assertEquals(org, fs.getParent(juzu));
      Iterator<P> juzuChildren = fs.getChildren(fs.asDir(juzu));
      assertTrue(juzuChildren.hasNext());
      P a = juzuChildren.next();
      assertFalse(juzuChildren.hasNext());

      //
      assertFalse(fs.isDir(a));
      assertTrue(fs.isFile(a));
      assertEquals("A.java", fs.getName(a));
      assertEquals(juzu, fs.getParent(a));
   }
}
