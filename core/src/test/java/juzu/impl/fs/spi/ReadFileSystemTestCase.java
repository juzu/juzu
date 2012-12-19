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

import juzu.test.AbstractTestCase;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ReadFileSystemTestCase extends AbstractTestCase {

  @Test
  public void testWar1() throws Exception {
    File war = File.createTempFile("foo", ".war");
    war.deleteOnExit();
    ShrinkWrap.create(WebArchive.class).
        addAsLibrary(ShrinkWrap.create(JavaArchive.class, "foo.jar").
            addClass(ReadFileSystemTestCase.class)).
        as(ZipExporter.class).
        exportTo(war, true);

    //
    URL url = new URL("jar:" + war.toURI().toURL() + "!/WEB-INF/lib/foo.jar");
    assertJar(url);
    assertFS(url);
  }


  @Test
  public void testWar2() throws Exception {
    File tmp = File.createTempFile("juzu", "juzu");
    assertTrue(tmp.delete());
    tmp.deleteOnExit();
    File war = new File(tmp, "foo.war");
    File lib = new File(war, "WEB-INF/lib");
    assertTrue(lib.mkdirs());
    File jar = new File(lib, "foo.jar");
    ShrinkWrap.create(JavaArchive.class).addClass(ReadFileSystemTestCase.class).as(ZipExporter.class).exportTo(jar);

    //
    URL url = jar.toURI().toURL();
    assertJar(url);
    assertFS(url);
  }

  @Test
  public void testWar3() throws Exception {
    File tmp = File.createTempFile("juzu", "juzu");
    assertTrue(tmp.delete());
    assertTrue(tmp.mkdirs());
    tmp.deleteOnExit();
    ShrinkWrap.create(WebArchive.class, "foo.war").
        addAsLibrary(ShrinkWrap.create(JavaArchive.class, "foo.jar").
            addClass(ReadFileSystemTestCase.class)).
        as(ExplodedExporter.class).
        exportExploded(tmp);
    File jar = new File(tmp, "foo.war/WEB-INF/lib/foo.jar");
    assertTrue(jar.exists());
    assertTrue(jar.isDirectory());

    //
    URL url = jar.toURI().toURL();
    assertFS(url);
  }

  private void assertJar(URL url) throws IOException {
    URLConnection conn = url.openConnection();
    conn.connect();
    InputStream in = conn.getInputStream();
    JavaArchive archive = ShrinkWrap.create(ZipImporter.class).importFrom(in).as(JavaArchive.class);
    Node node = archive.get("/" + ReadFileSystemTestCase.class.getName().replace('.', '/') + ".class");
    assertNotNull(node);
  }

  private void assertFS(URL url) throws IOException {
    ReadFileSystem fs = ReadFileSystem.create(url);
    Object path = fs.getPath("juzu", "impl", "fs", "spi", "ReadFileSystemTestCase.class");
    assertNotNull(path);
  }
}
