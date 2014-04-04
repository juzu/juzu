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

package juzu.impl.plugin.application;

import juzu.impl.compiler.CompilationError;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.request.ControllerHandler;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTestCase extends AbstractTestCase {

  @Test
  public void testDefaultController() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.application.default_controller");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.application.default_controller.Application");
    Class<?> aClass = compiler.assertClass("plugin.application.default_controller.A");

    //
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controller = new ControllersDescriptor(desc);
    assertSame(aClass, controller.getDefaultController());
  }

  public void _testMethodId() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.application.method.id");
    compiler.assertCompile();
    Class<?> appClass = compiler.assertClass("plugin.application.method.id.Application");
    Class<?> aClass = compiler.assertClass("plugin.application.method.id.A");

    //
    ApplicationDescriptor desc = ApplicationDescriptor.create(appClass);
    ControllersDescriptor controllerDesc = new ControllersDescriptor(desc);
    ControllerHandler a = controllerDesc.getHandler(aClass, "a");
    ControllerHandler b = controllerDesc.getHandler(aClass, "b");
    ControllerHandler c = controllerDesc.getHandler(aClass, "c");

    //
    assertEquals("foo", a.getId());
    assertEquals("bar", b.getId());
    assertEquals("juu", c.getId());

    //
    assertSame(a, controllerDesc.getMethodById("foo"));
    assertSame(b, controllerDesc.getMethodById("bar"));
    assertSame(c, controllerDesc.getMethodById("juu"));
  }

  public void _testDuplicateMethod() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.application.method.duplicate");
    List<CompilationError> errors = compiler.failCompile();
    assertEquals("Was expecting a single error instead of " + errors, 1, errors.size());
    assertEquals("/plugin/application/method/duplicate/A.java", errors.get(0).getSource());
  }

  public void _testPrefix() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.application.prefix");
    compiler.assertCompile();
  }

  @Test
  public void testMultiple() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.application.multiple");
    compiler.assertCompile();

    //
    Class<?> app1Class = compiler.assertClass("plugin.application.multiple.app1.Application");
    Class<?> a1Class = compiler.assertClass("plugin.application.multiple.app1.A");
    ApplicationDescriptor desc1 = ApplicationDescriptor.create(app1Class);
    ControllersDescriptor controllerDesc1 = new ControllersDescriptor(desc1);
    assertSame(a1Class, controllerDesc1.getControllers().get(0).getType());

    //
    Class<?> app2Class = compiler.assertClass("plugin.application.multiple.app2.Application");
    Class<?> a2Class = compiler.assertClass("plugin.application.multiple.app2.A");
    ApplicationDescriptor desc2 = ApplicationDescriptor.create(app2Class);
    ControllersDescriptor controllerDesc2 = new ControllersDescriptor(desc2);
    assertSame(a2Class, controllerDesc2.getControllers().get(0).getType());
  }
}
