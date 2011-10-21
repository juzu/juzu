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

package org.juzu.impl.application;

import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   public void testDefaultController() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "default_controller");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.default_controller.Default_controllerApplication");
      Class<?> aClass = compiler.assertClass("application.default_controller.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      assertSame(aClass, desc.getDefaultController());
   }
}
