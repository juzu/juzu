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

package org.juzu.impl.application.simple;

import junit.framework.TestCase;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.application.PhaseLiteral;
import org.juzu.impl.application.ApplicationProcessor;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.request.ControllerParameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SimpleApplicationTestCase extends TestCase
{

   public void testEmptyPackage() throws Exception
   {

      RAMFileSystem in = new RAMFileSystem();
      RAMDir root = in.getRoot();
      RAMDir foo = root.addDir("foo");
      RAMFile fooPkgInfo = foo.addFile("package-info.java").update(
         "@Application\n" +
         "package foo;\n" +
         "import org.juzu.Application;"
      );
      foo.addFile("A.java").update(
         "package foo;\n" +
         "import org.juzu.Render;\n" +
         "public class A {\n" +
         "@Render\n" +
         "public void render(String name) { }\n" +
         "}\n"
      );

      //
      RAMFileSystem out = new RAMFileSystem();

      //
      Compiler<RAMPath, RAMPath> compiler = new Compiler<RAMPath, RAMPath>(in, out);
      compiler.addAnnotationProcessor(new ApplicationProcessor());
      assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());

      //
      ClassLoader cl = new URLClassLoader(new URL[]{out.getURL()}, Thread.currentThread().getContextClassLoader());
      Class aClass = cl.loadClass("foo.A");

      //
      Class a_Class = cl.loadClass("foo.A_");
      Field f = a_Class.getDeclaredField("render");
      PhaseLiteral l = (PhaseLiteral)f.get(null);
      assertNotNull(l);
      ControllerMethod d = l.getDescriptor();
      assertSame(aClass, d.getType());
      assertSame("render", d.getMethodName());
      Method method = d.getMethod();
      assertEquals("render", method.getName());
      assertSame(aClass, method.getDeclaringClass());
      assertEquals(Arrays.<Class<?>>asList((Class)String.class), Arrays.asList(method.getParameterTypes()));
      assertEquals(Arrays.asList(new ControllerParameter("name")), d.getArgumentParameters());

      //
      Class applicationClass = cl.loadClass("foo.FooApplication");
      Method renderMethod = applicationClass.getMethod("renderURL", String.class);
      assertEquals("renderURL", renderMethod.getName());
   }
}
