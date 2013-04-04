/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.controller;

import juzu.impl.inject.Scoped;
import juzu.impl.inject.spi.InjectorProvider;
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

  public ScopeTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testRequestScope() throws Exception {
    MockApplication<?> app = application("plugin.controller.scope.request").init();

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
    MockApplication<?> app = application("plugin.controller.scope.flash").init();

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
    MockApplication<?> app = application("plugin.controller.scope.session").init();

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
