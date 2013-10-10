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
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClassLoaderTestCase extends AbstractTestCase {

  /** . */
  private File targetDir;

  /** . */
  private JavaArchive classes;

  /** . */
  private JavaArchive lib;

  /** . */
  private File libFile;

  /** . */
  private ClassLoader effectiveLoader;

  @Override
  public void setUp() throws Exception {
    String targetPath = System.getProperty("targetDir");
    assertNotNull(targetPath);
    File targetDir = new File(targetPath);
    assertTrue(targetDir.isDirectory());


    // Build a correct parent CL and check everything first
    JavaArchive classes = ShrinkWrap.create(JavaArchive.class).addClass(Dev.class).addAsResource(new StringAsset("classes_resource_value"), "classes_resource");
    JavaArchive lib = ShrinkWrap.create(JavaArchive.class).addClass(Lib.class).addAsResource(new StringAsset("lib_resource_value"), "lib_resource");
    File explodedDir = explode(targetDir, classes, lib);
    File libFile = new File(explodedDir, "WEB-INF/lib/lib.jar");
    File classesDir = new File(explodedDir, "WEB-INF/classes");
    URLClassLoader cl = new URLClassLoader(new URL[]{classesDir.toURI().toURL(), libFile.toURI().toURL()}, getExtClassLoader());
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
    this.targetDir = targetDir;
    this.effectiveLoader = cl;
    this.lib = lib;
    this.classes = classes;
    this.libFile = libFile;
  }

  private ClassLoader getExtClassLoader() {
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

  private File explode(File targetDir, JavaArchive classes, JavaArchive lib) {
    WebArchive archive = ShrinkWrap.create(WebArchive.class);
    archive.merge(classes, "WEB-INF/classes");
    archive.addAsDirectory("WEB-INF/lib");
    File explodedDir = archive.as(ExplodedExporter.class).exportExploded(targetDir);
    File libJar = new File(explodedDir, "WEB-INF/lib/lib.jar");
    lib.as(ZipExporter.class).exportTo(libJar);
    return explodedDir;
  }

  @Test
  public void testLoadingLoadingFromParent() throws Exception {
    // We cannot load from file
    ClassLoader cl = new ParentJarClassLoader(effectiveLoader);
    try {
      cl.loadClass(Dev.class.getName());
      fail();
    }
    catch (ClassNotFoundException e) {
    }
    URL classesResource = cl.getResource("classes_resource");
    assertNull(classesResource);
    assertFalse(cl.getResources("classes_resource").hasMoreElements());
    assertEquals(Collections.emptyList(), Tools.list(cl.getResources("classes_resource")));

    // We can load from jar
    assertSame(effectiveLoader.loadClass(Lib.class.getName()), cl.loadClass(Lib.class.getName()));
    URL libResource = cl.getResource("lib_resource");
    assertNotNull(libResource);
    assertEquals("lib_resource_value", Tools.read(libResource));
    Tools.list(cl.getResources("lib_resource").hasMoreElements());
    assertEquals(Collections.singletonList(libResource), Tools.list(cl.getResources("lib_resource")));
  }

  @Test
  public void testLoadingFromAncestor() throws Exception {
    ClassLoader cl = new ParentJarClassLoader(new URLClassLoader(new URL[0], effectiveLoader));

    // We can load from file
    assertSame(cl.loadClass(Dev.class.getName()), effectiveLoader.loadClass(Dev.class.getName()));
    URL classesResource = cl.getResource("classes_resource");
    assertNotNull(classesResource);
    assertEquals(Collections.singletonList(classesResource), Tools.list(cl.getResources("classes_resource")));

    // We can load from jar
    assertSame(effectiveLoader.loadClass(Lib.class.getName()), cl.loadClass(Lib.class.getName()));
    URL libResource = cl.getResource("lib_resource");
    assertNotNull(libResource);
    assertEquals("lib_resource_value", Tools.read(libResource));
    assertEquals(Collections.singletonList(libResource), Tools.list(cl.getResources("lib_resource")));
  }
}
