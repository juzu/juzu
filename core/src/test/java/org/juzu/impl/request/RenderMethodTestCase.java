/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.request;

import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.controller.descriptor.ControllerParameter;
import org.juzu.impl.utils.Cardinality;
import org.juzu.request.Phase;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderMethodTestCase extends AbstractTestCase
{

   @Override
   protected void setUp() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("request", "method", "render");
      compiler.assertCompile();
      aClass = compiler.assertClass("request.method.render.A");
      compiler.assertClass("request.method.render.A_");

      //
      Class<?> appClass = compiler.assertClass("request.method.render.RenderApplication");
      descriptor = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
   }

   /** . */
   private Class<?> aClass;

   /** . */
   private ApplicationDescriptor descriptor;

   public void testNoArg() throws Exception
   {
      ControllerMethod cm = descriptor.getControllerMethod(aClass, "noArg");
      assertEquals("noArg", cm.getName());
      assertEquals(Phase.RENDER, cm.getPhase());
      assertEquals(Collections.<ControllerParameter>emptyList(), cm.getArguments());
   }

   public void testStringArg() throws Exception
   {
      ControllerMethod cm = descriptor.getControllerMethod(aClass, "oneArg", String.class);
      assertEquals("oneArg", cm.getName());
      assertEquals(Phase.RENDER, cm.getPhase());
      assertEquals(Arrays.asList(new ControllerParameter("foo", Cardinality.SINGLE)), cm.getArguments());
   }
}
