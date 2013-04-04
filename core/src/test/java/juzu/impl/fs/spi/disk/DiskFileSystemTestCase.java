/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.fs.spi.disk;

import juzu.impl.fs.spi.AbstractReadWriteFileSystemTestCase;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DiskFileSystemTestCase extends AbstractReadWriteFileSystemTestCase<File> {

  @Override
  protected ReadWriteFileSystem<File> create() throws IOException {
    File root = File.createTempFile("juzu", "test");
    assertTrue(root.delete());
    assertTrue(root.mkdir());
    root.deleteOnExit();
    return new DiskFileSystem(root);
  }

  @Test
  public void testFoo() throws Exception {
    File root = new File(System.getProperty("juzu.test.resources.path"));
    assertNotNull(root);
    assertNotNull(root.isDirectory());

    //
    doTest(new DiskFileSystem(root, "compiler.disk"), root);
  }

  private <P> void doTest(ReadFileSystem<P> fs, P root) throws IOException {

    assertEquals(root, fs.getRoot());

    //
    assertTrue(fs.isDir(root));
    assertFalse(fs.isFile(root));
    assertEquals("", fs.getName(root));
    Iterator<P> rootChildren = fs.getChildren(root);
    assertTrue(rootChildren.hasNext());
    P compiler = rootChildren.next();
    assertFalse(rootChildren.hasNext());

    //
    assertTrue(fs.isDir(compiler));
    assertFalse(fs.isFile(compiler));
    assertEquals("compiler", fs.getName(compiler));
    Iterator<P> compilerChildren = fs.getChildren(compiler);
    assertTrue(compilerChildren.hasNext());
    P disk = compilerChildren.next();
    assertFalse(compilerChildren.hasNext());

    //
    assertTrue(fs.isDir(disk));
    assertFalse(fs.isFile(disk));
    assertEquals("disk", fs.getName(disk));
    Iterator<P> diskChildren = fs.getChildren(disk);
    assertTrue(diskChildren.hasNext());
    P a = diskChildren.next();
    assertFalse(diskChildren.hasNext());

    //
    assertFalse(fs.isDir(a));
    assertTrue(fs.isFile(a));
    assertEquals("A.java", fs.getName(a));
  }
}
