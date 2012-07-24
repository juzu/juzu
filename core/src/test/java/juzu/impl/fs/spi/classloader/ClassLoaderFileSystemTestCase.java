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

package juzu.impl.fs.spi.classloader;

import juzu.impl.fs.spi.SimpleFileSystem;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import sun.net.www.protocol.foo.Handler;

import javax.portlet.Portlet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClassLoaderFileSystemTestCase extends AbstractTestCase {

  /** . */
  private JavaArchive jar;

  {
    JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
    jar.setManifest(new StringAsset(""));
    jar.addAsResource(new StringAsset("bar.txt_value"), "bar.txt");
    jar.addAsResource(new StringAsset("foo/bar.txt_value"), "foo/bar.txt");
    jar.addAsResource(new StringAsset("foo/bar/juu.txt_value"), "foo/bar/juu.txt");

    //
    this.jar = jar;
  }

  @Test
  public void testJarFile() throws Exception {
    File f = File.createTempFile("test", ".jar");
    f.deleteOnExit();
    jar.as(ZipExporter.class).exportTo(f, true);
    assertFS(f.toURI().toURL());
  }

  @Test
  public void testJarStream() throws Exception {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      jar.as(ZipExporter.class).exportTo(baos);
      byte[] bytes = baos.toByteArray();
      Handler.bind("jarfile", bytes);
      URL url = new URL("foo:jarfile");

      //
      final String abc = url.toString();
      final URL manifestURL = new URL("jar:" + abc + "!/META-INF/MANIFEST.MF");
      final URL fooURL = new URL("jar:" + abc + "!/foo/");
      final URL barTxtURL = new URL("jar:" + abc + "!/foo/bar.txt");
      final URL barURL = new URL("jar:" + abc + "!/foo/bar/");
      final URL juuTxtURL = new URL("jar:" + abc + "!/foo/bar/juu.txt");

      //
      ClassLoader cl = new ClassLoader(ClassLoader.getSystemClassLoader()) {
        @Override
        protected URL findResource(String name) {
          if ("META-INF/MANIFEST.MF".equals(name)) {
            return manifestURL;
          }
          else if ("foo/".equals(name)) {
            return fooURL;
          }
          else if ("foo/bar.txt".equals(name)) {
            return barTxtURL;
          }
          else if ("foo/bar/juu.txt".equals(name)) {
            return barURL;
          }
          else if ("foo/bar/juu.txt".equals(name)) {
            return juuTxtURL;
          }
          return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
          Vector<URL> v = new Vector<URL>();
          URL url = findResource(name);
          if (url != null) {
            v.add(url);
          }
          return v.elements();
        }
      };

      assertFS(cl);
    }
    finally {
      Handler.clear();
    }
  }

  @Test
  public void testFile() throws Exception {
    File f = File.createTempFile("test", "");
    assertTrue(f.delete());
    assertTrue(f.mkdirs());
    f.deleteOnExit();
    File dir = jar.as(ExplodedExporter.class).exportExploded(f);
    assertFS(dir.toURI().toURL());
  }

  @Test
  public void testPortletJar() throws Exception {
    URL url = Portlet.class.getProtectionDomain().getCodeSource().getLocation();
    ClassLoader cl = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
    ClassLoaderFileSystem fs = new ClassLoaderFileSystem(cl);
    Object s = fs.getPath("javax", "portlet");
    assertNotNull(s);
  }

  private void assertFS(URL base) throws Exception {
    assertFS(new URLClassLoader(new URL[]{base}, ClassLoader.getSystemClassLoader()));
  }

  private void assertFS(ClassLoader classLoader) throws Exception {
    assertFS(new ClassLoaderFileSystem(classLoader));
  }

  private <P> void assertFS(SimpleFileSystem<P> fs) throws Exception {
    P foo = fs.getPath("foo");
    assertEquals("foo", fs.getName(foo));
    assertEquals("foo", fs.packageOf(foo, '.', new StringBuilder()).toString());
    ArrayList<? extends P> fooChildren = Tools.list(fs.getChildren(foo));
    assertEquals(1, fooChildren.size());
    P fooChild = fooChildren.get(0);
    assertEquals("foo", fs.packageOf(fooChild, '/', new StringBuilder()).toString());
    assertEquals("bar.txt", fs.getName(fooChild));

    //
    P fooBar = fs.getPath("foo", "bar.txt");
    assertEquals("bar.txt", fs.getName(fooBar));
    assertEquals("foo", fs.packageOf(fooBar, '.', new StringBuilder()).toString());
    URL fooBarURL = fs.getURL(fooBar);
    String fooBarContent = Tools.read(fooBarURL);
    assertEquals("foo/bar.txt_value", fooBarContent);

    //
    P fooBarJuu = fs.getPath("foo", "bar", "juu.txt");
    assertEquals("juu.txt", fs.getName(fooBarJuu));
    assertEquals("foo.bar", fs.packageOf(fooBarJuu, '.', new StringBuilder()).toString());
    URL fooBarJuuURL = fs.getURL(fooBarJuu);
    String fooBarJuuContent = Tools.read(fooBarJuuURL);
    assertEquals("foo/bar/juu.txt_value", fooBarJuuContent);

    //
    assertEquals(null, fs.getPath("juu"));
  }
}
