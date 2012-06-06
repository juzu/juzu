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

package juzu.impl.request;

import org.junit.Test;
import juzu.impl.spi.inject.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockActionBridge;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestContextTestCase extends AbstractInjectTestCase
{

   public RequestContextTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testInjection() throws Exception
   {
      MockApplication<?> app = application("request", "context").init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("render_phase", render.assertStringResult());

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
