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

package juzu.impl.application;

import juzu.impl.compiler.CompilationError;
import juzu.impl.controller.descriptor.MethodDescriptor;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTestCase extends AbstractTestCase {

  @Test
  public void testDefaultController() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("application", "default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("application.default_controller.Default_controllerApplication");
    Class<?> aClass = compiler.assertClass("application.default_controller.A");

    //
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    assertSame(aClass, desc.getControllers().getDefault());
  }

  public void _testMethodId() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("application", "method", "id");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("application.method.id.IdApplication");
    Class<?> aClass = compiler.assertClass("application.method.id.A");

    //
    ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
    MethodDescriptor a = desc.getControllers().getMethod(aClass, "a");
    MethodDescriptor b = desc.getControllers().getMethod(aClass, "b");
    MethodDescriptor c = desc.getControllers().getMethod(aClass, "c");

    //
    assertEquals("foo", a.getId());
    assertEquals("bar", b.getId());
    assertEquals("juu", c.getId());

    //
    assertSame(a, desc.getControllers().getMethodById("foo"));
    assertSame(b, desc.getControllers().getMethodById("bar"));
    assertSame(c, desc.getControllers().getMethodById("juu"));
  }

  public void _testDuplicateMethod() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("application", "method", "duplicate");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting a single error instead of " + errors, 1, errors.size());
    assertEquals("/application/method/duplicate/A.java", errors.get(0).getSource());
  }

  public void _testPrefix() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("application", "prefix");
    compiler.assertCompile();
  }
}
