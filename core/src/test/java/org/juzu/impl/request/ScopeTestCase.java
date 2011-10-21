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

import org.juzu.test.AbstractTestCase;
import org.juzu.test.Registry;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;
import org.juzu.test.request.MockRenderBridge;
import org.juzu.test.support.Car;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeTestCase extends AbstractTestCase
{

   public void testRenderScope() throws Exception
   {
      MockApplication<?> app = application("request", "scope", "render");

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals(1, render.getAttributes().size());
      long identity = Registry.<Long>unset("car");
      Car car = (Car)render.getAttributes().values().iterator().next();
      assertEquals(car.getIdentityHashCode(), identity);

      //
      client.invoke(Registry.<String>unset("action"));
      assertNull(Registry.get("car"));

      //
      client.invoke(Registry.<String>unset("resource"));
      assertNull(Registry.get("car"));
   }

   public void testFlashScope() throws Exception
   {
      MockApplication<?> app = application("request", "scope", "flash");

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      long identity1 = Registry.<Long>unset("car");
      assertEquals(1, client.getFlash(1).size());
      Car car1 = (Car)client.getFlash(1).values().iterator().next();
      assertEquals(car1.getIdentityHashCode(), identity1);

      //
      client.invoke(Registry.<String>unset("action"));
      long identity2 = Registry.<Long>unset("car");
      assertNotSame(identity1, identity2);
      assertEquals(1, client.getFlash(0).size());
      Car car2 = (Car)client.getFlash(0).values().iterator().next();
      assertNotSame(car1, car2);

      //
      client.render();
      long identity3 = Registry.<Long>unset("car");
      assertEquals(identity2, identity3);
      assertEquals(1, client.getFlash(1).size());
      Car car3 = (Car)client.getFlash(1).values().iterator().next();
      assertSame(car2, car3);
   }
}
