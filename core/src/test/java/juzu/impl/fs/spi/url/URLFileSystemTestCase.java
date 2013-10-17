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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

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
  public void testJarEntry() throws Exception {
    File tmp = File.createTempFile("juzu", ".jar");
    tmp.deleteOnExit();
    FileOutputStream baos = new FileOutputStream(tmp);
    jar.as(ZipExporter.class).exportTo(baos);
    URL url = new URL("jar:" + tmp.toURI().toURL() + "!/");
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

    //
    assertEquals("", fs.getName(fs.getRoot()));
    assertEquals(Collections.emptyList(), fs.getNames(fs.getRoot()));

    //
    P foo = fs.getPath("foo");
    assertEquals("foo", fs.getName(foo));
    HashSet<? extends P> fooChildren = Tools.set(fs.getChildren(foo));
    assertEquals(2, fooChildren.size());

    //
    P fooBarTxt = fs.getChild(foo, "bar.txt");
    assertTrue(fooChildren.contains(fooBarTxt));
    assertEquals("bar.txt", fs.getName(fooBarTxt));
    assertTrue(fs.isFile(fooBarTxt));
    URL fooBarTxtURL = fs.getURL(fooBarTxt);
    String fooBarTxtContent = Tools.read(fooBarTxtURL);
    assertEquals("foo/bar.txt_value", fooBarTxtContent);

    //
    P fooBar = fs.getChild(foo, "bar");
    assertTrue(fooChildren.contains(fooBar));
    assertEquals("bar", fs.getName(fooBar));
    assertTrue(fs.isDir(fooBar));

    //
    P fooBarJuu = fs.getPath("foo", "bar", "juu.txt");
    assertEquals("juu.txt", fs.getName(fooBarJuu));
    URL fooBarJuuURL = fs.getURL(fooBarJuu);
    String fooBarJuuContent = Tools.read(fooBarJuuURL);
    assertEquals("foo/bar/juu.txt_value", fooBarJuuContent);
    assertEquals(Tools.list("foo", "bar", "juu.txt"), fs.getNames(fooBarJuu));

    //
    assertEquals(null, fs.getPath("juu"));
  }

  @Test
  public void testNestedJarEntry() throws Exception {
    File tmp = File.createTempFile("juzu", ".jar");
    tmp.deleteOnExit();
    FileOutputStream baos = new FileOutputStream(tmp);
    jar.as(ZipExporter.class).exportTo(baos);
    URL url = new URL("jar:" + tmp.toURI().toURL() + "!/foo/");
    URLFileSystem fs = new URLFileSystem();
    fs.add(url);
    Node root = fs.getRoot();
    HashSet<Node> children = Tools.set(fs.getChildren(root));
    assertEquals(2, children.size());
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
