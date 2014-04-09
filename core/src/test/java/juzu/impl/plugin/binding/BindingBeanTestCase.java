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

package juzu.impl.plugin.binding;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingBeanTestCase extends AbstractInjectTestCase {

  public BindingBeanTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testCreate() throws Exception {
    MockApplication<?> app = application("plugin.binding.create").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    assertEquals("pass", render.assertStringResponse());
  }

  @Test
  public void testScope() throws Exception {
    MockApplication<?> app = application("plugin.binding.scope").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    String url = render.assertStringResponse();
    assertNotSame("", url);

    //
    render = (MockViewBridge)client.invoke(url);
    String result = render.assertStringResponse();
    assertEquals("pass", result);
  }
}
