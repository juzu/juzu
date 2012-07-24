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

import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFileSystemTestCase extends AbstractTestCase {

  @Test
  public void testLastModified() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    RAMFile fooTxt = fs.addFile(fs.getRoot(), "foo.txt").update("abc");
    long now = waitForOneMillis();
    assertTrue(fs.getLastModified(fooTxt) < now);
    waitForOneMillis();
    fooTxt.update("def");
    assertTrue(now < fs.getLastModified(fooTxt));
  }

  @Test
  public void testCopy() throws IOException {
    RAMFileSystem src = new RAMFileSystem();
    src.addFile(src.getRoot(), "foo").update("foo1");
    src.addDir(src.getRoot(), "bar");
    src.addFile(src.getRoot(), "juu").update("juu1");
    src.addDir(src.getRoot(), "bii");
    src.addFile(src.getRoot(), "baa");
    RAMFileSystem dst = new RAMFileSystem();

    //
    RAMPath dstRoot = dst.getRoot();
    dstRoot.addFile("juu").update("juu2");
    dstRoot.addDir("daa");
    dstRoot.addFile("bii");
    dstRoot.addDir("baa");

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
}
