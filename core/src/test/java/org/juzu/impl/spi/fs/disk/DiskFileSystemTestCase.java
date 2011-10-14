package org.juzu.impl.spi.fs.disk;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.ReadFileSystem;

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
      doTest(new DiskFileSystem(root, "compiler", "disk"), root);
   }
   
   private <P> void doTest(ReadFileSystem<P> fs, P root) throws IOException
   {
      
      assertEquals(root, fs.getRoot());

      //
      assertTrue(fs.isDir(root));
      assertFalse(fs.isFile(root));
      assertEquals("", fs.getName(root));
      assertEquals(null, fs.getParent(root));
      Iterator<P> rootChildren = fs.getChildren(root);
      assertTrue(rootChildren.hasNext());
      P compiler = rootChildren.next();
      assertFalse(rootChildren.hasNext());

      //
      assertTrue(fs.isDir(compiler));
      assertFalse(fs.isFile(compiler));
      assertEquals("compiler", fs.getName(compiler));
      assertEquals(root, fs.getParent(compiler));
      Iterator<P> compilerChildren = fs.getChildren(compiler);
      assertTrue(compilerChildren.hasNext());
      P disk = compilerChildren.next();
      assertFalse(compilerChildren.hasNext());

      //
      assertTrue(fs.isDir(disk));
      assertFalse(fs.isFile(disk));
      assertEquals("disk", fs.getName(disk));
      assertEquals(compiler, fs.getParent(disk));
      Iterator<P> diskChildren = fs.getChildren(disk);
      assertTrue(diskChildren.hasNext());
      P a = diskChildren.next();
      assertFalse(diskChildren.hasNext());

      //
      assertFalse(fs.isDir(a));
      assertTrue(fs.isFile(a));
      assertEquals("A.java", fs.getName(a));
      assertEquals(disk, fs.getParent(a));
   }
}
