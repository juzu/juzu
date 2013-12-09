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

package juzu.impl.fs.spi;

import juzu.impl.common.Resource;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractReadWriteFileSystemTestCase<P> extends AbstractTestCase {

  /**
   * Create a new blank file system.
   *
   * @return the new file system
   */
  protected abstract ReadWriteFileSystem<P> create() throws IOException;

  @Test
  public void testGetNames() throws IOException {
    ReadWriteFileSystem<P> fs = create();
    assertFalse(fs.getNames(fs.getRoot()).iterator().hasNext());
    List<String> expected = Tools.list("a", "b", "c");
    P path = fs.makePath(expected);
    List<String> test = Tools.list(fs.getNames(path));
    assertEquals(expected, test);
  }

  @Test
  public void testLifeCycle() throws Exception {
    ReadWriteFileSystem<P> fs = create();

    // The root exist
    P foo = fs.makePath(Collections.singleton("foo"));
    P bar = fs.makePath(foo, "bar");
    assertEquals(1, fs.size(ReadFileSystem.PATH));

    // Now create
    fs.updateResource(bar, new Resource("FOO"));

    //
    assertEquals(3, fs.size(ReadFileSystem.PATH));
    assertEquals(2, fs.size(ReadFileSystem.DIR));
    assertEquals(1, fs.size(ReadFileSystem.FILE));
  }
}
