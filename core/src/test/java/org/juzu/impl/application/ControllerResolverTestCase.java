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
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.controller.ControllerResolver;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.utils.Tools;
import org.juzu.request.Phase;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerResolverTestCase extends AbstractTestCase
{

   /**
    * No method id specified resolves to the <code>index</code> method of the unique controller.
    *
    * @throws Exception any exception
    */
   public void testResolveIndex() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "resolver", "default_method");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_method.Default_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, null, Collections.<String>emptySet());
      assertEquals("index", method.getName());
   }

   /**
    * No method id specified finds ambiguous when it finds more than a unique <code>index</code>.
    *
    * @throws Exception any exception
    */
   public void testResolveAmbiguousIndex() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "resolver", "ambiguous_method");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.ambiguous_method.Ambiguous_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      try
      {
         resolver.resolve(Phase.RENDER, null, Collections.<String>emptySet());
         fail();
      }
      catch (AmbiguousResolutionException e)
      {
      }
   }

   /**
    * No method id specified resolves to the <code>index</code> method of the default specified controller.
    *
    * @throws Exception any exception
    */
   public void testDefaultControllerResolveIndex() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "resolver", "default_controller");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_controller.Default_controllerApplication");
      Class<?> aClass = compiler.assertClass("application.resolver.default_controller.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, null, Collections.<String>emptySet());
      assertEquals("index", method.getName());
      assertSame(aClass, method.getMethod().getDeclaringClass());
   }


   /**
    * Test method overloading resolution.
    *
    * @throws Exception any exception
    */
   public void testOverload() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "resolver", "overload");
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.overload.OverloadApplication");
      Class<?> aClass = compiler.assertClass("application.resolver.overload.A");
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);

      //
      ControllerMethod method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set());
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set(), method.getArgumentNames());

      //
      method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set("foo"));
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set("foo"), method.getArgumentNames());

      //
      method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set("foo", "bar"));
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

      //
      method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set("bar"));
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

      //
      method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set("bar"));
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set("foo", "bar"), method.getArgumentNames());

      //
      method = resolver.resolve(Phase.RENDER, "A.m", Tools.<String>set("daa"));
      assertEquals("m", method.getName());
      assertEquals(Tools.<String>set(), method.getArgumentNames());
   }



   public void testResolution() throws Exception
   {
      CompilerHelper<?, ?> compiler = compiler("application", "resolver", "method");
      compiler.assertCompile();

      //
      Class<?> aClass = compiler.assertClass("application.resolver.method.A");
      Class<?> clazz = compiler.assertClass("application.resolver.method.MethodApplication");
      ApplicationDescriptor desc = (ApplicationDescriptor)clazz.getField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod cm1_ = desc.getControllerMethod(aClass, "noArg");
      ControllerMethod cm2_ = desc.getControllerMethod(aClass, "fooArg", String.class);

      //
      ControllerMethod cm1 = resolver.resolve(Phase.RENDER, cm1_.getId(), cm1_.getArgumentNames());
      assertNotNull(cm1);
      assertEquals("noArg", cm1.getName());

      //
      ControllerMethod cm2 = resolver.resolve(Phase.RENDER, cm2_.getId(), cm2_.getArgumentNames());
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
}
