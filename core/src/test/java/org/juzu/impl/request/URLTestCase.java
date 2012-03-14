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

import org.juzu.URLBuilder;
import org.juzu.impl.utils.JSON;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.protocol.mock.MockApplication;
import org.juzu.test.protocol.mock.MockClient;
import org.juzu.test.protocol.mock.MockRenderBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLTestCase extends AbstractInjectTestCase
{

   public void testSimple() throws Exception
   {
      MockApplication<?> app = application("request", "url", "simple");
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      JSON url = (JSON)JSON.parse(render.assertStringResult());
      assertFalse(url.getJSON("properties").contains(URLBuilder.ESCAPE_XML.class.getName()));
   }

   public void testEscapeXML() throws Exception
   {
      MockApplication<?> app = application("request", "url", "escapexml");
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      JSON url = (JSON)JSON.parse(render.assertStringResult());
      assertEquals(Boolean.TRUE, url.getJSON("properties").get(URLBuilder.ESCAPE_XML.class.getName()));
   }

   public void testInvalidProperty() throws Exception
   {
      MockApplication<?> app = application("request", "url", "invalidproperty");
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("pass", render.assertStringResult());
   }
}
