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

package juzu.impl.plugin.controller;

import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.Method;
import juzu.impl.common.Tools;
import juzu.request.Phase;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResolverTestCase extends AbstractTestCase {

  /**
   * No method id specified resolves to the <code>index</code> method of the unique controller.
   *
   * @throws Exception any exception
   */
  @Test
  public void testResolveIndex() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.default_method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_method.Application");

    //
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();
    Method method = resolver.resolve(Phase.VIEW, Collections.<String>emptySet());
    assertEquals("index", method.getName());
  }

  /**
   * No method id specified finds ambiguous when it finds more than a unique <code>index</code>.
   *
   * @throws Exception any exception
   */
  @Test
  public void testResolveAmbiguousIndex() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.ambiguous_method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.ambiguous_method.Application");

    //
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();
    try {
      resolver.resolve(Phase.VIEW, Collections.<String>emptySet());
      fail();
    }
    catch (AmbiguousResolutionException e) {
    }
  }

  /**
   * No method id specified resolves to the <code>index</code> method of the default specified controller.
   *
   * @throws Exception any exception
   */
  @Test
  public void testDefaultControllerResolveIndex() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_controller.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.default_controller.A");

    //
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();
    Method method = resolver.resolve(Phase.VIEW, Collections.<String>emptySet());
    assertEquals("index", method.getName());
    assertSame(aClass, method.getMethod().getDeclaringClass());
  }


  /**
   * Test method overloading resolution.
   *
   * @throws Exception any exception
   */
  @Test
  public void testOverload() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.overload");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.overload.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.overload.A");
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();

    //
    Method method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set());
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set(), method.getParameterNames());

    //
    method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set("foo"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo"), method.getParameterNames());

    //
    method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set("foo", "bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getParameterNames());

    //
    method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set("bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getParameterNames());

    //
    method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set("bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getParameterNames());

    //
    method = resolver.resolveMethod(Phase.VIEW, "A.m", Tools.<String>set("daa"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set(), method.getParameterNames());
  }

  @Test
  public void testResolution() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.method");
    compiler.assertCompile();

    //
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.method.A");
    Class<?> clazz = compiler.assertClass("plugin.controller.resolver.method.Application");
    ApplicationDescriptor desc = ApplicationDescriptor.create(clazz);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();
    Method cm1_ = controllerDesc.getMethod(aClass, "noArg");
    Method cm2_ = controllerDesc.getMethod(aClass, "fooArg", String.class);

    //
    Method cm1 = resolver.resolveMethod(Phase.VIEW, cm1_.getId(), cm1_.getParameterNames());
    assertNotNull(cm1);
    assertEquals("noArg", cm1.getName());

    //
    Method cm2 = resolver.resolveMethod(Phase.VIEW, cm2_.getId(), cm2_.getParameterNames());
    assertNotNull(cm2);
    assertEquals("fooArg", cm2.getName());
  }

  @Test
  public void testTemplate() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_controller.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.default_controller.A");
    Class<?> bClass = compiler.assertClass("plugin.controller.resolver.default_controller.B");
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();

    //
    Method method = resolver.resolve((String)null, "index", Collections.<String>emptySet());
    assertEquals("index", method.getName());
    assertSame(method.getType(), aClass);

    //
    method = resolver.resolve("A", "index", Collections.<String>emptySet());
    assertEquals("index", method.getName());
    assertSame(method.getType(), aClass);

    //
    method = resolver.resolve("B", "index", Collections.<String>emptySet());
    assertEquals("index", method.getName());
    assertSame(method.getType(), bClass);
  }

  @Test
  public void testTemplateResolveMethod() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.resolver.method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.method.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.method.A");
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerResolver<Method> resolver = controllerDesc.getResolver();

    //
    Method method = resolver.resolve((String)null, "noArg", Collections.<String>emptySet());
    assertEquals("noArg", method.getName());
    assertSame(method.getType(), aClass);
    //
    method = resolver.resolve((String)null, "fooArg", Collections.<String>emptySet());
    assertEquals("fooArg", method.getName());
    assertSame(method.getType(), aClass);

    //
    method = resolver.resolve((String)null, "fooArg", Collections.<String>singleton("foo"));
    assertEquals("fooArg", method.getName());
    assertSame(method.getType(), aClass);

    //
    method = resolver.resolve((String)null, "fooArg", Collections.<String>singleton("bar"));
    assertNull(method);
  }
}
