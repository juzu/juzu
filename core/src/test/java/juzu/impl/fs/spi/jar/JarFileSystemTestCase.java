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

package juzu.impl.fs.spi.jar;

import juzu.impl.fs.Visitor;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JarFileSystemTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws Exception {
    URL url = Test.class.getProtectionDomain().getCodeSource().getLocation();
    System.out.println("url = " + url);
    JarFile file = new JarFile(new File(url.toURI()));
    final JarFileSystem filesystem = new JarFileSystem(file);
    filesystem.traverse(new Visitor.Default<JarPath>() {
      @Override
      public void enterDir(JarPath dir, String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        filesystem.packageOf(dir, '/', sb);
        System.out.println("dir " + sb);
      }
    });
  }

  @Test
  public void testBaseURL() throws Exception {
    URL url = Test.class.getProtectionDomain().getCodeSource().getLocation();
    JarFile file = new JarFile(new File(url.toURI()));
    JarFileSystem filesystem = new JarFileSystem(file);
    URL baseURL = filesystem.getURL();
    assertNotNull(baseURL);
  }
}
