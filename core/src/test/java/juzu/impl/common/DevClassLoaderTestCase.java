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

package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DevClassLoaderTestCase extends AbstractTestCase {

  /** . */
  private File targetDir;

  @Override
  public void setUp() {
    String targetPath = System.getProperty("targetDir");
    assertNotNull(targetPath);
    File targetDir = new File(targetPath);
    assertTrue(targetDir.isDirectory());

    //
    this.targetDir = targetDir;
  }

  private ClassLoader getParentClassLoader() {
    ClassLoader systemCL = ClassLoader.getSystemClassLoader();
    ClassLoader extCL = systemCL.getParent();
    try {
      extCL.loadClass(Dev.class.getName());
      fail();
    }
    catch (ClassNotFoundException e) {
    }
    try {
      extCL.loadClass(Lib.class.getName());
      fail();
    }
    catch (ClassNotFoundException e) {
    }
    return extCL;
  }

  private File explode(JavaArchive classes, JavaArchive lib) {
    WebArchive archive = ShrinkWrap.create(WebArchive.class);
    archive.merge(classes, "WEB-INF/classes");
    archive.addAsDirectory("WEB-INF/lib");
    File explodedDir = archive.as(ExplodedExporter.class).exportExploded(targetDir);
    File libJar = new File(explodedDir, "WEB-INF/lib/lib.jar");
    lib.as(ZipExporter.class).exportTo(libJar);
    return explodedDir;
  }

  private File archive(JavaArchive classes, JavaArchive lib) {
    try {
      WebArchive archive = ShrinkWrap.create(WebArchive.class);
      archive.merge(classes, "WEB-INF/classes");
      archive.addAsLibrary(lib);
      File tmp = File.createTempFile("archive", ".war", targetDir);
      archive.as(ZipExporter.class).exportTo(tmp, true);
      return tmp;
    }
    catch (IOException e) {
      throw failure(e);
    }
  }

  @Test
  public void testLoad() throws Exception {
    JavaArchive classes = ShrinkWrap.create(JavaArchive.class).addClass(Dev.class).addAsResource(new StringAsset("classes_resource_value"), "classes_resource");
    JavaArchive lib = ShrinkWrap.create(JavaArchive.class).addClass(Lib.class).addAsResource(new StringAsset("lib_resource_value"), "lib_resource");
    File explodedDir = explode(classes, lib);

    //
    File libJar = new File(explodedDir, "WEB-INF/lib/lib.jar");
    File classesDir = new File(explodedDir, "WEB-INF/classes");

    // Build a correct parent CL
    URLClassLoader cl = new URLClassLoader(new URL[]{classesDir.toURI().toURL(), libJar.toURI().toURL()}, getParentClassLoader());
    Class<?> devClass = cl.loadClass(Dev.class.getName());
    assertNotSame(devClass, Dev.class);
    Class<?> libClass = cl.loadClass(Lib.class.getName());
    assertNotSame(libClass, Lib.class);
    InputStream classesResource = cl.getResourceAsStream("classes_resource");
    assertNotNull(classesResource);
    assertEquals("classes_resource_value", Tools.read(classesResource));
    InputStream libResource = cl.getResourceAsStream("lib_resource");
    assertNotNull(libResource);
    assertEquals("lib_resource_value", Tools.read(libResource));

    //
    DevClassLoader devCL = new DevClassLoader(cl);
    try {
      devCL.loadClass(Dev.class.getName());
      fail();
    }
    catch (ClassNotFoundException e) {
    }
    assertSame(libClass, devCL.loadClass(Lib.class.getName()));
    classesResource = devCL.getResourceAsStream("classes_resource");
    assertNull(classesResource);
    libResource = devCL.getResourceAsStream("lib_resource");
    assertNotNull(libResource);
    assertEquals("lib_resource_value", Tools.read(libResource));
  }

  @Test
  public void testShadowedResource() throws Exception {
    JavaArchive classes = ShrinkWrap.create(JavaArchive.class).addAsResource(new StringAsset("classes_resource_value"), "resource");
    JavaArchive lib = ShrinkWrap.create(JavaArchive.class).addClass(Lib.class).addAsResource(new StringAsset("lib_resource_value"), "resource");
    File explodedDir = explode(classes, lib);

    //
    File libJar = new File(explodedDir, "WEB-INF/lib/lib.jar");
    File classesDir = new File(explodedDir, "WEB-INF/classes");

    // Build correct parent CL
    ClassLoader cl = new URLClassLoader(new URL[]{classesDir.toURI().toURL(), libJar.toURI().toURL()}, getParentClassLoader());

    //
    DevClassLoader devCL = new DevClassLoader(cl);
    URL url = devCL.getResource("resource");
    assertNotNull(url);
    assertEquals("lib_resource_value", Tools.read(url));

    //
    Enumeration<URL> e = devCL.getResources("resource");
    assertTrue(e.hasMoreElements());
    url = e.nextElement();
    assertNotNull(url);
    assertEquals("lib_resource_value", Tools.read(url));
    assertFalse(e.hasMoreElements());
  }
}
