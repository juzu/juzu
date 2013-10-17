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

import japa.parser.ast.type.ClassOrInterfaceType;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import juzu.test.JavaFile;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/** @author Julien Viet */
public class LiveClassLoaderTestCase extends AbstractTestCase {

  private class Context {

    /** . */
    final String name;

    /** . */
    CompilerAssert<File, File> compilerAssert1;

    /** . */
    CompilerAssert<File, File> compilerAssert2;

    /** . */
    URLClassLoader parent;

    /** . */
    LiveClassLoader local;

    private Context(String name) {
      this.name = name;
      this.compilerAssert1 = compiler(name);
      this.compilerAssert2 = compiler(name);
      this.parent = null;
      this.local = null;
    }

    void aExtends(String name, String type) {
      JavaFile<?> file = compilerAssert2.assertJavaSource("common.live." + name + ".A");
      file.assertDeclaration().setExtends(Arrays.asList(new ClassOrInterfaceType(type)));
      file.assertSave();
    }

    void init() {
      if (local == null)
      try {
        compilerAssert1.assertCompile();
        compilerAssert2.assertCompile();
        parent = new URLClassLoader(new URL[]{compilerAssert1.getClassOutput().getURL()});
        local = new LiveClassLoader(new URL[]{compilerAssert2.getClassOutput().getURL()}, parent);
      }
      catch (IOException e) {
        throw failure(e);
      }
    }

    Class<?> assertLoadedBy(ClassLoader expectedLoader, String name) {
      try {
        Class<?> clazz = local.loadClass(name);
        assertSame(expectedLoader, clazz.getClassLoader());
        return clazz;
      }
      catch (ClassNotFoundException e) {
        throw failure(e);
      }
    }

    Class<?> assertLoadedByParent(String name) {
      return assertLoadedBy(parent, name);
    }

    Class<?> assertLoadedLocally(String name) {
      return assertLoadedBy(local, name);
    }
  }

  @Test
  public void testNotModified() throws Exception {
    Context ctx = new Context("common.live.notmodified");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedByParent("common.live.notmodified.A");
  }

  @Test
  public void testModified() throws Exception {
    Context ctx = new Context("common.live.modified");
    JavaFile<?> file = ctx.compilerAssert2.assertJavaSource("common.live.modified.A");
    file.assertDeclaration().setExtends(Arrays.asList(new ClassOrInterfaceType("java.util.Date")));
    file.assertSave();
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.modified.A");
  }

  @Test
  public void testDependsOnModified() throws Exception {
    Context ctx = new Context("common.live.dependsonmodified");
    JavaFile<?> file = ctx.compilerAssert2.assertJavaSource("common.live.dependsonmodified.B");
    file.assertDeclaration().setExtends(Arrays.asList(new ClassOrInterfaceType("java.util.Date")));
    file.assertSave();
    ctx.init();

    //
    Class<?> d = ctx.assertLoadedLocally("common.live.dependsonmodified.D");
    Class<?> c = ctx.assertLoadedLocally("common.live.dependsonmodified.C");
    Class<?> b = ctx.assertLoadedLocally("common.live.dependsonmodified.B");
    Class<?> a = ctx.assertLoadedByParent("common.live.dependsonmodified.A");

    //
    assertSame(a, b.getDeclaredField("ref").getType());
    assertSame(b, c.getDeclaredField("ref").getType());
    assertSame(c, d.getDeclaredField("ref").getType());
  }

  @Test
  public void testCircularNonModified() throws Exception {
    Context ctx = new Context("common.live.circular");
    ctx.init();

    //
    Class<?> b = ctx.assertLoadedByParent("common.live.circular.B");
    Class<?> a = ctx.assertLoadedByParent("common.live.circular.A");

    //
    assertSame(b, a.getDeclaredField("ref").getType());
    assertSame(a, b.getDeclaredField("ref").getType());
  }

  @Test
  public void testCircular() throws Exception {
    Context ctx = new Context("common.live.circular");
    ctx.aExtends("circular", "java.util.Date");
    ctx.init();

    //
    Class<?> b = ctx.assertLoadedLocally("common.live.circular.B");
    Class<?> a = ctx.assertLoadedLocally("common.live.circular.A");

    //
    assertSame(b, a.getDeclaredField("ref").getType());
    assertSame(a, b.getDeclaredField("ref").getType());
  }

  @Test
  public void testNotFound() {
    LiveClassLoader loader = new LiveClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
    try {
      loader.loadClass("foo.bar");
      fail();
    }
    catch (ClassNotFoundException ok) {
    }
  }

  @Test
  public void testFoundLocally() throws Exception {
    Context ctx = new Context("common.live.foundlocally");
    ctx.compilerAssert1.assertJavaSource("common.live.foundlocally.B").assertRemove();
    ctx.init();

    //
    Class<?> b = ctx.assertLoadedLocally("common.live.foundlocally.B");
    Class<?> a = ctx.assertLoadedByParent("common.live.foundlocally.A");

    //
    assertSame(a, b.getDeclaredField("ref").getType());
  }

  @Test
  public void testMethodReturnTypeModified() throws Exception {
    Context ctx = new Context("common.live.methodreturntypemodified");
    ctx.aExtends("methodreturntypemodified", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodreturntypemodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodreturntypemodified.B");

    //
    assertSame(a, b.getDeclaredMethod("m").getReturnType());
  }

  @Test
  public void testMethodParameterTypeModified() throws Exception {
    Context ctx = new Context("common.live.methodparametertypemodified");
    ctx.aExtends("methodparametertypemodified", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodparametertypemodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodparametertypemodified.B");

    //
    assertSame(a, b.getDeclaredMethods()[0].getParameterTypes()[0]);
  }

  @Test
  public void testConstructorParameterTypeModified() throws Exception {
    Context ctx = new Context("common.live.constructorparametertypemodified");
    ctx.aExtends("constructorparametertypemodified", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.constructorparametertypemodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.constructorparametertypemodified.B");

    //
    assertSame(a, b.getDeclaredConstructors()[0].getParameterTypes()[0]);
  }

  @Test
  public void testSuperClassModified() throws Exception {
    Context ctx = new Context("common.live.superclassmodified");
    ctx.aExtends("superclassmodified", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.superclassmodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.superclassmodified.B");

    //
    assertSame(a, b.getSuperclass());
  }

  @Test
  public void testImplementedIntefaceModified() throws Exception {
    Context ctx = new Context("common.live.implementedinterfacemodified");
    ctx.aExtends("implementedinterfacemodified", "java.io.Serializable");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.implementedinterfacemodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.implementedinterfacemodified.B");

    //
    assertSame(a, b.getInterfaces()[0]);
  }

  @Test
  public void testSuperIntefaceModified() throws Exception {
    Context ctx = new Context("common.live.superinterfacemodified");
    ctx.aExtends("superinterfacemodified", "java.io.Serializable");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.superinterfacemodified.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.superinterfacemodified.B");

    //
    assertSame(a, b.getInterfaces()[0]);
  }

  @Test
  public void testSuperTypeParameterModified1() throws Exception {
    Context ctx = new Context("common.live.supertypeparametermodified1");
    ctx.aExtends("supertypeparametermodified1", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.supertypeparametermodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.supertypeparametermodified1.B");

    //
    assertSame(a, ((ParameterizedType)b.getGenericSuperclass()).getActualTypeArguments()[0]);
  }

  @Test
  public void testSuperTypeParameterModified2() throws Exception {
    Context ctx = new Context("common.live.supertypeparametermodified2");
    ctx.aExtends("supertypeparametermodified2", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.supertypeparametermodified2.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.supertypeparametermodified2.B");

    //
    assertSame(a, ((ParameterizedType)((ParameterizedType)b.getGenericSuperclass()).getActualTypeArguments()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testSuperInterfaceParameterModified1() throws Exception {
    Context ctx = new Context("common.live.superinterfaceparametermodified1");
    ctx.aExtends("superinterfaceparametermodified1", "java.io.Serializable");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.superinterfaceparametermodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.superinterfaceparametermodified1.B");

    //
    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testMethodGenericParameterTypeModified1() throws Exception {
    Context ctx = new Context("common.live.methodgenericparametertypemodified1");
    ctx.aExtends("methodgenericparametertypemodified1", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified1.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testMethodGenericParameterTypeModified2() throws Exception {
    Context ctx = new Context("common.live.methodgenericparametertypemodified2");
    ctx.aExtends("methodgenericparametertypemodified2", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified2.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified2.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testMethodGenericParameterTypeModified3() throws Exception {
    Context ctx = new Context("common.live.methodgenericparametertypemodified3");
    ctx.aExtends("methodgenericparametertypemodified3", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified3.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodgenericparametertypemodified3.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testMethodGenericReturnTypeModified1() throws Exception {
    Context ctx = new Context("common.live.methodgenericreturntypemodified1");
    ctx.aExtends("methodgenericreturntypemodified1", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.methodgenericreturntypemodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.methodgenericreturntypemodified1.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testConstructorGenericParameterTypeModified1() throws Exception {
    Context ctx = new Context("common.live.constructorgenericparametertypemodified1");
    ctx.aExtends("constructorgenericparametertypemodified1", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.constructorgenericparametertypemodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.constructorgenericparametertypemodified1.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testFieldGenericTypeModified1() throws Exception {
    Context ctx = new Context("common.live.fieldgenerictypemodified1");
    ctx.aExtends("fieldgenerictypemodified1", "java.util.Date");
    ctx.init();

    //
    Class<?> a = ctx.assertLoadedLocally("common.live.fieldgenerictypemodified1.A");
    Class<?> b = ctx.assertLoadedLocally("common.live.fieldgenerictypemodified1.B");

    //
//    assertSame(a, ((ParameterizedType)b.getGenericInterfaces()[0]).getActualTypeArguments()[0]);
  }

  @Test
  public void testResource() throws IOException {
    Context ctx = new Context("common.live.resource");
    ctx.init();

    //
    assertEquals("bar", Tools.read(ctx.local.getResource("common/live/resource/foo.txt")));

    //
    File dir = new File(ctx.compilerAssert2.getClassOutput().getRoot(), "common/live/resource");
    assertTrue(dir.mkdirs());
    File f = new File(dir, "foo.txt");
    new FileWriter(f).append("bar_").close();
    assertEquals("bar_", Tools.read(ctx.local.getResource("common/live/resource/foo.txt")));
  }
}
