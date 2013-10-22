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
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import juzu.test.protocol.mock.MockResourceBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceTestCase extends AbstractInjectTestCase {

  public ResourceTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testNotFound() throws Exception {
    MockApplication<?> app = application("plugin.controller.resource.notfound").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockResourceBridge resource = (MockResourceBridge)client.invoke(render.assertStringResult());
    resource.assertNotFound();
  }

  @Test
  public void testBinary() throws Exception {
    MockApplication<?> app = application("plugin.controller.resource.binary").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockResourceBridge resource = (MockResourceBridge)client.invoke(render.assertStringResult());
    assertEquals("hello", new String(resource.assertBinaryResult(), "UTF-8"));
    assertEquals("application/octet-stream", resource.getMimeType());
  }
}
