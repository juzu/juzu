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

import juzu.impl.common.Content;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScannerTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<String[]> scanner = FileSystemScanner.createTimestamped(fs);
    
    //
    Snapshot<String[]> snapshot = scanner.take();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());

    //
    String[] foo = fs.makePath(fs.getRoot(), "foo");
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());

    //
    String[] bar = fs.makePath(foo, "bar.txt");
    fs.setContent(bar, new Content(""));
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.singletonMap("/foo/bar.txt", Change.ADD), snapshot.getChanges());
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());

    //
    fs.setContent(bar, new Content("value"));
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.singletonMap("/foo/bar.txt", Change.UPDATE), snapshot.getChanges());

    //
    fs.removePath(bar);
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.singletonMap("/foo/bar.txt", Change.REMOVE), snapshot.getChanges());
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());
  }

  @Test
  public void testIgnoreHiddenFile() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<String[]> scanner = FileSystemScanner.createTimestamped(fs);

    //
    Snapshot<String[]> snapshot = scanner.take();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());
    String[] foo = fs.makePath(fs.getRoot(), ".foo");
    fs.setContent(foo, new Content(""));
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());
  }

  @Test
  public void testIgnoreHiddenDir() throws IOException {
    RAMFileSystem fs = new RAMFileSystem();
    FileSystemScanner<String[]> scanner = FileSystemScanner.createTimestamped(fs);

    //
    Snapshot<String[]> snapshot = scanner.take();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());
    String[] bar = fs.makePath(fs.makePath(fs.getRoot(), ".foo"), "bar.txt");
    fs.setContent(bar, new Content(""));
    waitForOneMillis();
    snapshot = snapshot.scan();
    assertEquals(Collections.<String, Change>emptyMap(), snapshot.getChanges());
  }
}
