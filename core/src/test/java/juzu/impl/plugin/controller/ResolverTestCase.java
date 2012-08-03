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

package juzu.impl.plugin.controller;

import juzu.AmbiguousResolutionException;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
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
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "default_method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_method.Application");

    //
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();
    MethodDescriptor method = resolver.resolve(Collections.<String>emptySet());
    assertEquals("index", method.getName());
  }

  /**
   * No method id specified finds ambiguous when it finds more than a unique <code>index</code>.
   *
   * @throws Exception any exception
   */
  @Test
  public void testResolveAmbiguousIndex() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "ambiguous_method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.ambiguous_method.Application");

    //
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();
    try {
      resolver.resolve(Collections.<String>emptySet());
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
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_controller.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.default_controller.A");

    //
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();
    MethodDescriptor method = resolver.resolve(Collections.<String>emptySet());
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
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "overload");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.overload.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.overload.A");
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();

    //
    MethodDescriptor method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set());
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set(), method.getArgumentNames());

    //
    method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set("foo"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo"), method.getArgumentNames());

    //
    method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set("foo", "bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

    //
    method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set("bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

    //
    method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set("bar"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

    //
    method = resolver.resolve(Phase.VIEW, "A.m", Tools.<String>set("daa"));
    assertEquals("m", method.getName());
    assertEquals(Tools.<String>set(), method.getArgumentNames());
  }

  @Test
  public void testResolution() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "method");
    compiler.assertCompile();

    //
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.method.A");
    Class<?> clazz = compiler.assertClass("plugin.controller.resolver.method.Application");
    ApplicationDescriptor desc = (ApplicationDescriptor)clazz.getField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();
    MethodDescriptor cm1_ = desc.getControllers().getMethod(aClass, "noArg");
    MethodDescriptor cm2_ = desc.getControllers().getMethod(aClass, "fooArg", String.class);

    //
    MethodDescriptor cm1 = resolver.resolve(Phase.VIEW, cm1_.getId(), cm1_.getArgumentNames());
    assertNotNull(cm1);
    assertEquals("noArg", cm1.getName());

    //
    MethodDescriptor cm2 = resolver.resolve(Phase.VIEW, cm2_.getId(), cm2_.getArgumentNames());
    assertNotNull(cm2);
    assertEquals("fooArg", cm2.getName());
  }

  @Test
  public void testTemplate() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.default_controller.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.default_controller.A");
    Class<?> bClass = compiler.assertClass("plugin.controller.resolver.default_controller.B");
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();

    //
    MethodDescriptor method = resolver.resolve((String)null, "index", Collections.<String>emptySet());
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
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "resolver", "method");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.controller.resolver.method.Application");
    Class<?> aClass = compiler.assertClass("plugin.controller.resolver.method.A");
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    ControllerResolver<MethodDescriptor> resolver = desc.getControllers().getResolver();

    //
    MethodDescriptor method = resolver.resolve((String)null, "noArg", Collections.<String>emptySet());
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
