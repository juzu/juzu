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

import juzu.Response;
import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.List;

/** @author Julien Viet */
public class ControllerTestCase extends AbstractInjectTestCase {

  public ControllerTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testAbstract() throws Exception {
    List<CompilationError> app = compiler("plugin.controller.abstract_").formalErrorReporting().failCompile();
    assertEquals(1, app.size());
    CompilationError error = app.get(0);
    assertEquals(ControllerMetaModel.CONTROLLER_IS_ABSTRACT, error.getCode());
    assertTrue("Was expecting source to end with abstract_/A.java", error.getSource().endsWith("abstract_/A.java"));
  }

  public static Object shared;

  @Test
  public void testErrorHandler() throws Exception {
    Object ret = invokeErrorHandler("plugin.controller.error.invoke");
    assertInstanceOf(ConcurrentModificationException.class, assertInstanceOf(Response.Error.class, ret).getCause());
  }

  @Test
  public void testErrorHandlerInjection() throws Exception {
    Object ret = invokeErrorHandler("plugin.controller.error.inject");
    assertEquals("plugin.controller.error.inject.A", ret.getClass().getName());
  }

  private Object invokeErrorHandler(String pkg) throws Exception {
    MockApplication app = application(pkg).init();
    MockClient client = app.client();
    shared = null;
    MockViewBridge render = client.render();
    render.assertOk();
    render.assertStringResponse("hello");
    assertNotNull(shared);
    return shared;
  }
}
