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

import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodTestCase extends AbstractTestCase
{

   public void testId() throws Exception
   {
      CompilerHelper<File, RAMPath> compiler = compiler("application", "method", "id");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.method.id.IdApplication");
      Class<?> aClass = compiler.assertClass("application.method.id.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerMethod a = desc.getControllerMethod(aClass, "a");
      ControllerMethod b = desc.getControllerMethod(aClass, "b");
      ControllerMethod c = desc.getControllerMethod(aClass, "c");

      //
      assertEquals("foo", a.getId());
      assertEquals("bar", b.getId());
      assertEquals("juu", c.getId());

      //
      assertSame(a, desc.getControllerMethodById("foo"));
      assertSame(b, desc.getControllerMethodById("bar"));
      assertSame(c, desc.getControllerMethodById("juu"));
   }

   public void testDuplicate() throws Exception
   {
      CompilerHelper<File, RAMPath> compiler = compiler("application", "method", "duplicate");
      List<CompilationError> errors = compiler.failCompile();
      assertEquals(1, errors.size());
      assertEquals("/application/method/duplicate/A.java", errors.get(0).getSource());
   }
}
