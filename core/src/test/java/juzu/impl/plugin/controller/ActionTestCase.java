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

import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockActionBridge;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionTestCase extends AbstractInjectTestCase {

  public ActionTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testNoOp() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.noop").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockActionBridge action = (MockActionBridge)client.invoke(render.assertStringResult());
    action.assertNoResponse();
  }

  @Test
  public void testRedirect() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.redirect").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockActionBridge action = (MockActionBridge)client.invoke(render.assertStringResult());
    action.assertRedirect("http://www.julienviet.com");
  }

  @Test
  public void testRender() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.render").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockActionBridge action = (MockActionBridge)client.invoke(render.assertStringResult());
    action.assertRender("render", Collections.singletonMap("arg", "arg_value"));
  }
}
