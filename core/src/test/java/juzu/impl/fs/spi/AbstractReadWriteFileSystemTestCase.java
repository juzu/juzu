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

package juzu.impl.fs.spi;

import juzu.impl.common.Content;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractReadWriteFileSystemTestCase<P> extends AbstractTestCase {

  /**
   * Create a new blank file system.
   *
   * @return the new file system
   */
  protected abstract ReadWriteFileSystem<P> create() throws IOException;

  @Test
  public void testLifeCycle() throws Exception {
    ReadWriteFileSystem<P> fs = create();

    // The root exist
    P foo = fs.makePath(Collections.singleton("foo"));
    P bar = fs.makePath(foo, "bar");
    assertEquals(1, fs.size(ReadFileSystem.PATH));

    // Now create
    fs.setContent(bar, new Content("FOO"));

    //
    assertEquals(3, fs.size(ReadFileSystem.PATH));
    assertEquals(2, fs.size(ReadFileSystem.DIR));
    assertEquals(1, fs.size(ReadFileSystem.FILE));
  }
}
