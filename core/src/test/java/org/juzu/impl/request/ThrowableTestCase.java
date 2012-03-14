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

import org.junit.Test;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.protocol.mock.MockApplication;
import org.juzu.test.protocol.mock.MockClient;
import org.juzu.test.protocol.mock.MockRenderBridge;

import javax.naming.AuthenticationException;
import java.util.ConcurrentModificationException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ThrowableTestCase extends AbstractInjectTestCase
{
   public ThrowableTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testConstructorChecked() throws Exception
   {
      MockApplication<?> app = application("request", "constructor", "throwable", "checked").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }

   @Test
   public void testConstructorRuntime() throws Exception
   {
      MockApplication<?> app = application("request", "constructor", "throwable", "runtime").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }

   @Test
   public void testConstructorError() throws Exception
   {
      MockApplication<?> app = application("request", "constructor", "throwable", "error").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }

   @Test
   public void testRenderChecked() throws Exception
   {
      MockApplication<?> app = application("request", "render", "throwable", "checked").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }

   @Test
   public void testRenderRuntime() throws Exception
   {
      MockApplication<?> app = application("request", "render", "throwable", "runtime").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }

   @Test
   public void testRenderError() throws Exception
   {
      MockApplication<?> app = application("request", "render", "throwable", "error").init();

      //
      MockClient client = app.client();
      try
      {
         client.render();
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }

   @Test
   public void testActionChecked() throws Exception
   {
      MockApplication<?> app = application("request", "action", "throwable", "checked").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }

   @Test
   public void testActionRuntime() throws Exception
   {
      MockApplication<?> app = application("request", "action", "throwable", "runtime").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }

   @Test
   public void testActionError() throws Exception
   {
      MockApplication<?> app = application("request", "action", "throwable", "error").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }

   @Test
   public void testResourceChecked() throws Exception
   {
      MockApplication<?> app = application("request", "resource", "throwable", "checked").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }

   @Test
   public void testResourceRuntime() throws Exception
   {
      MockApplication<?> app = application("request", "resource", "throwable", "runtime").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }

   @Test
   public void testResourceError() throws Exception
   {
      MockApplication<?> app = application("request", "resource", "throwable", "error").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      try
      {
         client.invoke(render.assertStringResult());
         fail();
      }
      catch (ApplicationException e)
      {
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }
}
