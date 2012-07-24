/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.fs.spi.disk;

import juzu.impl.fs.spi.ReadFileSystem;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DiskFileSystemTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws Exception {
    File root = new File(System.getProperty("test.resources"));
    assertNotNull(root);
    assertNotNull(root.isDirectory());

    //
    doTest(new DiskFileSystem(root, "compiler", "disk"), root);
  }

  private <P> void doTest(ReadFileSystem<P> fs, P root) throws IOException {

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
