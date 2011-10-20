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

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.CompilerHelper;
import org.juzu.test.Registry;
import org.juzu.test.request.MockActionBridge;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;
import org.juzu.test.request.MockRenderBridge;
import org.juzu.test.support.Car;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestContextTestCase extends TestCase
{

   public void testInjection() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "request", "context");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      ClassLoader cl2 = new URLClassLoader(new URL[]{compiler.getOutput().getURL()}, Thread.currentThread().getContextClassLoader());

      //
      MockApplication<RAMPath> app = new MockApplication<RAMPath>(compiler.getOutput(), cl2);
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("render_phase", render.getContent());

      //
      String url = render.getTitle();
      MockActionBridge action = (MockActionBridge)client.invoke(url);
      //
//      client.
//      client.invoke();
//      assertNull(Registry.get("car"));

      //
//      client.invoke(Registry.<String>unset("resource"));
//      assertNull(Registry.get("car"));
   }
}
