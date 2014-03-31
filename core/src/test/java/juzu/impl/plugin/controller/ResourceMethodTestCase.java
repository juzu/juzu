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
import juzu.impl.request.ControlParameter;
import juzu.impl.request.Method;
import juzu.impl.request.PhaseParameter;
import juzu.impl.common.Cardinality;
import juzu.request.Phase;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceMethodTestCase extends AbstractTestCase {

  @Override
  public void setUp() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin.controller.method.resource");
    compiler.assertCompile();

    //
    aClass = compiler.assertClass("plugin.controller.method.resource.A");
    compiler.assertClass("plugin.controller.method.resource.A_");
    Class<?> appClass = compiler.assertClass("plugin.controller.method.resource.Application");
    descriptor = ApplicationDescriptor.create(appClass);
    controllerDescriptor = new ControllersDescriptor(descriptor);
  }

  /** . */
  private Class<?> aClass;

  /** . */
  private ApplicationDescriptor descriptor;

  /** . */
  private ControllersDescriptor controllerDescriptor;

  @Test
  public void testNoArg() throws Exception {
    Method cm = controllerDescriptor.getMethod(aClass, "noArg");
    assertEquals("noArg", cm.getName());
    assertEquals(Phase.RESOURCE, cm.getPhase());
    assertEquals(Collections.<ControlParameter>emptyList(), cm.getParameters());
  }

  @Test
  public void testStringArg() throws Exception {
    Method cm = controllerDescriptor.getMethod(aClass, "oneArg", String.class);
    assertEquals("oneArg", cm.getName());
    assertEquals(Phase.RESOURCE, cm.getPhase());
    assertEquals(Arrays.asList(new PhaseParameter("foo", null, null, Cardinality.SINGLE, null)), cm.getParameters());
  }
}
