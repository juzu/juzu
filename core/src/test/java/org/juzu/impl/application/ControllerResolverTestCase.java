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

import org.juzu.AmbiguousResolutionException;
import org.juzu.Phase;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerResolverTestCase extends AbstractTestCase
{

   public void testResolution() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "method");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      Class<?> aClass = compiler.assertClass("application.resolver.method.A");
      Class<?> clazz = compiler.assertClass("application.resolver.method.MethodApplication");
      ApplicationDescriptor desc = (ApplicationDescriptor)clazz.getField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod cm1_ = desc.getControllerMethod(aClass, "noArg");
      ControllerMethod cm2_ = desc.getControllerMethod(aClass, "fooArg", String.class);

      //
      ControllerMethod cm1 = resolver.resolve(Phase.RENDER, cm1_.getId());
      assertNotNull(cm1);
      assertEquals("noArg", cm1.getName());

      //
      ControllerMethod cm2 = resolver.resolve(Phase.RENDER, cm2_.getId());
      assertNotNull(cm2);
      assertEquals("fooArg", cm2.getName());

      //
//      try
//      {
//         resolver.resolve(Phase.RENDER, Builder.map("foo", new String[]{"foo_value"}).put("bar", new String[]{"bar_value"}).build());
//         fail();
//      }
//      catch (AmbiguousResolutionException ignore)
//      {
//      }
   }

   public void testResolverDefaultMethod() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "default_method");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_method.Default_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, null);
      assertEquals("index", method.getName());
   }

   public void testResolverAmbiguousMethod() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "ambiguous_method");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.ambiguous_method.Ambiguous_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      try
      {
         resolver.resolve(Phase.RENDER, null);
         fail();
      }
      catch (AmbiguousResolutionException e)
      {
      }
   }

   public void testResolverDefaultController() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "default_controller");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_controller.Default_controllerApplication");
      Class<?> aClass = compiler.assertClass("application.resolver.default_controller.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, null);
      assertEquals("index", method.getName());
      assertSame(aClass, method.getMethod().getDeclaringClass());
   }
}
