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

package juzu.impl.fs.spi.url;

import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.test.AbstractTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.portlet.Portlet;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLFileSystemTestCase extends AbstractTestCase {

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
  public void testJar() throws Exception {
    File tmp = File.createTempFile("juzu", ".jar");
    tmp.deleteOnExit();
    FileOutputStream baos = new FileOutputStream(tmp);
    jar.as(ZipExporter.class).exportTo(baos);
    URL url = new URL("jar:" + tmp.toURI().toURL() + "!/foo/bar.txt_value");
    assertFS(url);
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
  public void testFromClassLoaderWithJar() throws Exception {
    File f = File.createTempFile("test", ".jar");
    f.deleteOnExit();
    jar.as(ZipExporter.class).exportTo(f, true);

    //
    URLClassLoader loader = new URLClassLoader(new URL[]{f.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
    URLFileSystem fs = new URLFileSystem();
    fs.add(loader);
    assertFS(fs);
  }

  @Test
  public void testFromClassLoaderWithFile() throws Exception {
    File f = File.createTempFile("test", "");
    assertTrue(f.delete());
    assertTrue(f.mkdirs());
    f.deleteOnExit();
    File dir = jar.as(ExplodedExporter.class).exportExploded(f);

    //
    URLClassLoader loader = new URLClassLoader(new URL[]{dir.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
    URLFileSystem fs = new URLFileSystem();
    fs.add(loader);
    assertFS(fs);
  }

  private void assertFS(URL url) throws Exception {
    URLFileSystem fs = new URLFileSystem();
    fs.add(url);
    assertFS(fs);
  }

  private <P> void assertFS(ReadFileSystem<P> fs) throws Exception {
    P foo = fs.getPath("foo");
    assertEquals("foo", fs.getName(foo));
    ArrayList<? extends P> fooChildren = Tools.list(fs.getChildren(foo));
    assertEquals(1, fooChildren.size());
    P fooChild = fooChildren.get(0);
    assertEquals("bar.txt", fs.getName(fooChild));

    //
    P fooBar = fs.getPath("foo", "bar.txt");
    assertEquals("bar.txt", fs.getName(fooBar));
    URL fooBarURL = fs.getURL(fooBar);
    String fooBarContent = Tools.read(fooBarURL);
    assertEquals("foo/bar.txt_value", fooBarContent);

    //
    P fooBarJuu = fs.getPath("foo", "bar", "juu.txt");
    assertEquals("juu.txt", fs.getName(fooBarJuu));
    URL fooBarJuuURL = fs.getURL(fooBarJuu);
    String fooBarJuuContent = Tools.read(fooBarJuuURL);
    assertEquals("foo/bar/juu.txt_value", fooBarJuuContent);

    //
    assertEquals(null, fs.getPath("juu"));
  }


  @Test
  public void testPortletJar() throws Exception {
    URL url = Portlet.class.getProtectionDomain().getCodeSource().getLocation();
    URLFileSystem fs = new URLFileSystem();
    fs.add(url);
    Object s = fs.getPath("javax", "portlet");
    assertNotNull(s);
  }

  @Test
  public void testInheritance() throws Exception {
    URLClassLoader loader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
    URLFileSystem fs = new URLFileSystem();
    fs.add(loader, ClassLoader.getSystemClassLoader().getParent());

    //
    Node assertClass = fs.getPath("junit", "framework", "Assert.class");
    assertNotNull(assertClass);
  }
}
