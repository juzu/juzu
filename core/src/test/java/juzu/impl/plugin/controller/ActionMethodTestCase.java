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

import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.plugin.controller.descriptor.ParameterDescriptor;
import juzu.impl.common.Cardinality;
import juzu.request.Phase;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionMethodTestCase extends AbstractTestCase {

  @Override
  public void setUp() throws Exception {
    CompilerAssert<?, ?> compiler = compiler("plugin", "controller", "method", "action");
    compiler.assertCompile();
    aClass = compiler.assertClass("plugin.controller.method.action.A");
    compiler.assertClass("plugin.controller.method.action.A_");

    //
    Class<?> appClass = compiler.assertClass("plugin.controller.method.action.Application");
    descriptor = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
  }

  /** . */
  private Class<?> aClass;

  /** . */
  private ApplicationDescriptor descriptor;

  @Test
  public void testNoArg() throws Exception {
    MethodDescriptor cm = descriptor.getControllers().getMethod(aClass, "noArg");
    assertEquals("noArg", cm.getName());
    assertEquals(Phase.ACTION, cm.getPhase());
    assertEquals(Collections.<ParameterDescriptor>emptyList(), cm.getArguments());
  }

  @Test
  public void testStringArg() throws Exception {
    MethodDescriptor cm = descriptor.getControllers().getMethod(aClass, "oneArg", String.class);
    assertEquals("oneArg", cm.getName());
    assertEquals(Phase.ACTION, cm.getPhase());
    assertEquals(Arrays.asList(new ParameterDescriptor("foo", Cardinality.SINGLE)), cm.getArguments());
  }
}
