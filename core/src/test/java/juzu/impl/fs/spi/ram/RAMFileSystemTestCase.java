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

package juzu.impl.fs.spi.ram;

import juzu.impl.common.Content;
import juzu.impl.fs.spi.AbstractReadWriteFileSystemTestCase;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFileSystemTestCase extends AbstractReadWriteFileSystemTestCase<String[]> {

  @Override
  protected ReadWriteFileSystem<String[]> create() throws IOException {
    return new RAMFileSystem();
  }

  @Test
  public void testLastModified() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    String[] fooTxt = fs.makePath(fs.getRoot(), "foo.txt");
    fs.setContent(fooTxt, new Content("abc"));
    long now = waitForOneMillis();
    assertTrue(fs.getLastModified(fooTxt) < now);
    waitForOneMillis();
    fs.setContent(fooTxt, new Content("def"));
    assertTrue(now < fs.getLastModified(fooTxt));
  }

/*
  @Test
  public void testCopy() throws IOException {
    RAMFileSystem src = new RAMFileSystem();

    String[] foo = src.makePath(src.getRoot(), "foo");
    String[] bar = src.makePath(src.getRoot(), "bar");
    String[] juu = src.makePath(src.getRoot(), "juu");
    String[] bii = src.makePath(src.getRoot(), "bii");
    String[] baa = src.makePath(src.getRoot(), "baa");

    src.setContent(foo, new Content(System.currentTimeMillis(), "foo1"));
//    src.makeDir(src.getRoot(), "bar");
    src.setContent(juu, new Content(System.currentTimeMillis(), "juu1"));
    RAMFileSystem dst = new RAMFileSystem();

    //
    String[] dstRoot = dst.getRoot();
    dst.setContent(juu, new Content(System.currentTimeMillis(), "juu2"));

    //
    src.copy(dst);

    //
    RAMPath bar = dstRoot.getChild("bar");
    assertNotNull(bar);
    assertTrue(dst.isDir(bar));
    RAMPath foo = dstRoot.getChild("foo");
    assertNotNull(foo);
    assertTrue(dst.isFile(foo));
    List<RAMPath> children = Tools.list(dstRoot.getChildren());
    assertEquals(5, children.size());
    RAMFile juu = (RAMFile)dstRoot.getChild("juu");
    assertEquals("juu1", juu.getContent().getCharSequence().toString());
    assertEquals(src.getContent(src.getChild(src.getRoot(), "juu")).getLastModified(), juu.getContent().getLastModified());
    RAMPath bii = dstRoot.getChild("bii");
    assertNotNull(bii);
    assertTrue(bii instanceof RAMDir);
    RAMPath baa = dstRoot.getChild("baa");
    assertNotNull(baa);
    assertTrue(baa instanceof RAMFile);
  }
*/
}
