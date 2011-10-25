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

package org.juzu.impl.template;

import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;
import org.juzu.test.request.MockRenderBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TagTestCase extends AbstractInjectTestCase
{

   public void _testSimple() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "simple").init();
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.getContent();
      assertEquals("<foo>bar</foo>", out);
   }

   public void testDecorate() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "decorate").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.getContent();
      assertEquals("<foo>bar</foo>", out);
   }

   public void testInclude() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "resolve").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String out = render.getContent();
      assertEquals("foo", out);
   }

   public void testTitle() throws Exception
   {
      MockApplication<?> app = application("template", "tag", "title").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      String url = render.getContent();
      assertEquals("the_title", render.getTitle());
      render = (MockRenderBridge)client.invoke(url);
      assertEquals("4", render.getTitle());
   }
}
