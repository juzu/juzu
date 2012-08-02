/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.controller;

import juzu.impl.inject.Scoped;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.Identifiable;
import juzu.test.Registry;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRequestBridge;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeTestCase extends AbstractInjectTestCase {

  public ScopeTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testRequestScope() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "scope", "request").init();

    //
    MockClient client = app.client();

    //
    MockRequestBridge request = client.render();
    List<Scoped> attributes = request.getAttributesHistory();
    assertEquals(1, attributes.size());
    Identifiable car = (Identifiable)attributes.iterator().next().get();
    assertEquals(Identifiable.DESTROYED, car.getStatus());

    //
    long id1 = Registry.<Long>unset("car");
    assertEquals(car.getIdentityHashCode(), id1);
    assertEquals(Identifiable.MANAGED, (int)Registry.<Integer>unset("status"));

    //
    request = client.invoke(Registry.<String>unset("action"));
    attributes = request.getAttributesHistory();
    assertEquals(1, attributes.size());
    car = (Identifiable)attributes.iterator().next().get();
    assertEquals(Identifiable.DESTROYED, car.getStatus());

    //
    long id2 = Registry.<Long>unset("car");
    assertNotSame(id1, id2);
    assertEquals(id2, id2);
    assertEquals(Identifiable.MANAGED, (int)Registry.<Integer>unset("status"));

    //
    request = client.invoke(Registry.<String>unset("resource"));
    attributes = request.getAttributesHistory();
    assertEquals(1, attributes.size());
    car = (Identifiable)attributes.iterator().next().get();
    assertEquals(Identifiable.DESTROYED, car.getStatus());

    //
    long id3 = Registry.<Long>unset("car");
    assertNotSame(id1, id3);
    assertNotSame(id2, id3);
    assertEquals(car.getIdentityHashCode(), id3);
    assertEquals(Identifiable.MANAGED, (int)Registry.<Integer>unset("status"));
  }

  @Test
  public void testFlashScope() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "scope", "flash").init();

    //
    MockClient client = app.client();

    //
    client.render();
    long id1 = Registry.<Long>unset("car");
    int status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertEquals(1, client.getFlashHistory(1).size());
    Identifiable car1 = (Identifiable)client.getFlashHistory(1).iterator().next().get();
    assertEquals(car1.getIdentityHashCode(), id1);
    assertEquals(Identifiable.DESTROYED, car1.getStatus());

    //
    client.invoke(Registry.<String>unset("action"));
    long id2 = Registry.<Long>unset("car");
    status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertNotSame(id1, id2);
    assertEquals(1, client.getFlashHistory(0).size());
    Identifiable car2 = (Identifiable)client.getFlashHistory(0).iterator().next().get();
    assertNotSame(car1, car2);
    assertEquals(Identifiable.MANAGED, car2.getStatus());

    //
    client.render();
    long id3 = Registry.<Long>unset("car");
    status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertEquals(id2, id3);
    assertEquals(1, client.getFlashHistory(1).size());
    Identifiable car3 = (Identifiable)client.getFlashHistory(1).iterator().next().get();
    assertSame(car2, car3);
    assertEquals(Identifiable.DESTROYED, car2.getStatus());
  }

  @Test
  public void testSessionScope() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "scope", "session").init();

    //
    MockClient client = app.client();

    //
    client.render();
    long id1 = Registry.<Long>unset("car");
    int status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertEquals(1, client.getSession().size());
    Identifiable car1 = (Identifiable)client.getSession().iterator().next().get();
    assertEquals(car1.getIdentityHashCode(), id1);
    assertEquals(Identifiable.MANAGED, car1.getStatus());

    //
    client.invoke(Registry.<String>unset("action"));
    long id2 = Registry.<Long>unset("car");
    status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertNotSame(id1, id2);
    assertEquals(1, client.getSession().size());
    Identifiable car2 = (Identifiable)client.getSession().iterator().next().get();
    assertSame(car1, car2);
    assertEquals(Identifiable.MANAGED, car2.getStatus());

    //
    client.render();
    long id3 = Registry.<Long>unset("car");
    status = Registry.<Integer>unset("status");
    assertEquals(Identifiable.MANAGED, status);
    assertEquals(id2, id3);
    assertEquals(1, client.getSession().size());
    Identifiable car3 = (Identifiable)client.getSession().iterator().next().get();
    assertSame(car2, car3);
    assertEquals(Identifiable.MANAGED, car2.getStatus());
  }
}
