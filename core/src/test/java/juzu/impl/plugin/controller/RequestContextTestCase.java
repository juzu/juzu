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
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestContextTestCase extends AbstractInjectTestCase {

  public RequestContextTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testInjection() throws Exception {
    MockApplication<?> app = application("plugin.controller.context").init();

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
