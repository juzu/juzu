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

package juzu.impl.fs;

import juzu.impl.fs.spi.ram.RAMFile;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScannerTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<RAMPath> scanner = FileSystemScanner.createTimestamped(fs);

    //
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

    //
    RAMPath foo = fs.addDir(fs.getRoot(), "foo");
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

    //
    RAMFile bar = fs.addFile(foo, "bar.txt");
    waitForOneMillis();
    assertEquals(Collections.singletonMap("foo/bar.txt", Change.ADD), scanner.scan());
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

    //
    bar.update("value");
    waitForOneMillis();
    assertEquals(Collections.singletonMap("foo/bar.txt", Change.UPDATE), scanner.scan());
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());

    //
    bar.del();
    waitForOneMillis();
    assertEquals(Collections.singletonMap("foo/bar.txt", Change.REMOVE), scanner.scan());
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());
  }

  @Test
  public void testIgnoreHiddenFile() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<RAMPath> scanner = FileSystemScanner.createTimestamped(fs);

    //
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());
    fs.addFile(fs.getRoot(), ".foo");
    waitForOneMillis();
    assertEquals(Collections.emptyMap(), scanner.scan());
  }

  @Test
  public void testIgnoreHiddenDir() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<RAMPath> scanner = FileSystemScanner.createTimestamped(fs);

    //
    assertEquals(Collections.<String, Change>emptyMap(), scanner.scan());
    fs.addFile(fs.addDir(fs.getRoot(), ".foo"), "bar.txt");
    waitForOneMillis();
    assertEquals(Collections.emptyMap(), scanner.scan());
  }
}
