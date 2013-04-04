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
