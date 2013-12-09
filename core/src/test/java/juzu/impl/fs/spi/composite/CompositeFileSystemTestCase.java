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

package juzu.impl.fs.spi.composite;

import juzu.impl.common.Resource;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.PathType;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompositeFileSystemTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws IOException {

    RAMFileSystem ramFS = new RAMFileSystem();
    ramFS.updateResource(new String[]{"a", "b"}, new Resource("foo"));

    //
    CompositeFileSystem composite = new CompositeFileSystem(ramFS);

    Context root = composite.getRoot();
    Context a = composite.getChild(root, "a");
    assertNotNull(a);
    assertEquals(PathType.DIR, composite.typeOf(a));

    //
    Context b = composite.getChild(a, "b");
    assertNotNull(b);
    assertEquals(PathType.FILE, composite.typeOf(b));
    assertEquals(Tools.list("a", "b"), composite.getNames(b));

    //
    Timestamped<Resource> content = composite.getResource(b);
    assertNotNull(content);
    assertEquals("foo", content.getObject().getCharSequence().toString());




  }
}
