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

import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.protocol.mock.MockActionBridge;
import org.juzu.test.protocol.mock.MockApplication;
import org.juzu.test.protocol.mock.MockClient;
import org.juzu.test.protocol.mock.MockRenderBridge;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderTestCase extends AbstractInjectTestCase
{

   private static final Pattern P = Pattern.compile("([0-9]+)\\[(.*)\\]");

   public void testIndex() throws Exception
   {
      MockApplication<?> app = application("request", "render", "index").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("index", render.assertStringResult());
   }

   public void testParameterizedIndex() throws Exception
   {
      MockApplication<?> app = application("request", "render", "parameterizedindex").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      Matcher m = P.matcher(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("0", m.group(1));
      render = (MockRenderBridge)client.invoke(m.group(2));
      m.reset(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("1", m.group(1));
      render = (MockRenderBridge)client.invoke(m.group(2));
      m.reset(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("0", m.group(1));
   }

   public void testOverridenIndex() throws Exception
   {
      MockApplication<?> app = application("request", "render", "overridenindex").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      Matcher m = P.matcher(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("0", m.group(1));
      render = (MockRenderBridge)client.invoke(m.group(2));
      m.reset(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("1", m.group(1));
      render = (MockRenderBridge)client.invoke(m.group(2));
      m.reset(render.assertStringResult());
      assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
      assertEquals("0", m.group(1));
   }

   public void testResponse() throws Exception
   {
      MockApplication<?> app = application("request", "render", "response").init();

      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("foo", render.assertStringResult());
   }

   public void testUpdate() throws Exception
   {
      MockApplication<?> app = application("request", "render", "update").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String url = render.assertStringResult();
      MockActionBridge action = (MockActionBridge)client.invoke(url);
      action.assertRender("A.done", Collections.<String, String>emptyMap());

   }
}
