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
