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
package juzu.plugin.authz;

import juzu.Response;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

/**
 * @author Julien Viet
 */
public class AuthorizationTestCase extends AbstractTestCase {

  @Test
  public void testHandlerRolesAllowed() throws Exception {
    assertRolesAllowed("juzu.handler.rolesallowed");
  }

  @Test
  public void testHandlerPermitAll() throws Exception {
    assertPermitAll("juzu.handler.permitall");
  }

  @Test
  public void testHandlerDenyAll() throws Exception {
    assertDenyAll("juzu.handler.denyall");
  }

  @Test
  public void testOverridelRolesAllowed() throws Exception {
    assertRolesAllowed("juzu.override.rolesallowed");
  }

  @Test
  public void testOverridePermitAll() throws Exception {
    assertPermitAll("juzu.override.permitall");
  }

  @Test
  public void testOverrideDenyAll() throws Exception {
    assertDenyAll("juzu.override.denyall");
  }

  @Test
  public void testControllerRolesAllowed() throws Exception {
    assertRolesAllowed("juzu.controller.rolesallowed");
  }

  @Test
  public void testControllerPermitAll() throws Exception {
    assertPermitAll("juzu.controller.permitall");
  }

  private void assertRolesAllowed(String packageName) throws Exception {
    MockApplication<?> application = application(InjectorProvider.GUICE, packageName);
    application.init();
    MockClient client = application.client();
    MockViewBridge view1 = client.render();
    assertInstanceOf(Response.Error.Forbidden.class, view1.assertError());
    client.addRole("foo");
    MockViewBridge view2 = client.render();
    view2.assertOk();
  }

  private void assertPermitAll(String packageName) throws Exception {
    MockApplication<?> application = application(InjectorProvider.GUICE, packageName);
    application.init();
    MockClient client = application.client();
    MockViewBridge view1 = client.render();
    view1.assertOk();
    client.addRole("foo");
    MockViewBridge view2 = client.render();
    view2.assertOk();
  }

  private void assertDenyAll(String packageName) throws Exception {
    MockApplication<?> application = application(InjectorProvider.GUICE, packageName);
    application.init();
    MockClient client = application.client();
    MockViewBridge view1 = client.render();
    assertInstanceOf(Response.Error.Forbidden.class, view1.assertError());
    client.addRole("foo");
    MockViewBridge view2 = client.render();
    assertInstanceOf(Response.Error.Forbidden.class, view2.assertError());
  }
}
