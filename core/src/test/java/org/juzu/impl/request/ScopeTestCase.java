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
import org.juzu.test.Identifiable;
import org.juzu.test.Registry;
import org.juzu.test.protocol.mock.MockApplication;
import org.juzu.test.protocol.mock.MockClient;
import org.juzu.test.protocol.mock.MockRenderBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeTestCase extends AbstractInjectTestCase
{

   public void testRequestScope() throws Exception
   {
      MockApplication<?> app = application("request", "scope", "request");
      app.declareBean("request.scope.request.Car");
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals(1, render.getAttributes().size());
      long identity = Registry.<Long>unset("car");
      Identifiable car = (Identifiable)render.getAttributes().values().iterator().next().get();
      assertEquals(car.getIdentityHashCode(), identity);

      //
      client.invoke(Registry.<String>unset("action"));
      assertNotNull(Registry.get("car"));

      //
      client.invoke(Registry.<String>unset("resource"));
      assertNotNull(Registry.get("car"));
   }

   public void testFlashScope() throws Exception
   {
      MockApplication<?> app = application("request", "scope", "flash");
      app.declareBean("request.scope.flash.Car");
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      long identity1 = Registry.<Long>unset("car");
      assertEquals(1, client.getFlash(1).size());
      Identifiable car1 = (Identifiable)client.getFlash(1).values().iterator().next().get();
      assertEquals(car1.getIdentityHashCode(), identity1);

      //
      client.invoke(Registry.<String>unset("action"));
      long identity2 = Registry.<Long>unset("car");
      assertNotSame(identity1, identity2);
      assertEquals(1, client.getFlash(0).size());
      Identifiable car2 = (Identifiable)client.getFlash(0).values().iterator().next().get();
      assertNotSame(car1, car2);

      //
      client.render();
      long identity3 = Registry.<Long>unset("car");
      assertEquals(identity2, identity3);
      assertEquals(1, client.getFlash(1).size());
      Identifiable car3 = (Identifiable)client.getFlash(1).values().iterator().next().get();
      assertSame(car2, car3);
   }
}
