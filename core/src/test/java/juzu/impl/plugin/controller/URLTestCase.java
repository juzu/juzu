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

import juzu.PropertyType;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.JSON;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLTestCase extends AbstractInjectTestCase {

  public URLTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testSimple() throws Exception {
    MockApplication<?> app = application("plugin.controller.url.simple");
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    JSON url = (JSON)JSON.parse(render.assertStringResult());
    assertFalse(url.getJSON("properties").contains(PropertyType.ESCAPE_XML.getClass().getName()));
  }

  @Test
  public void testEscapeXML() throws Exception {
    MockApplication<?> app = application("plugin.controller.url.escapexml");
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    JSON url = (JSON)JSON.parse(render.assertStringResult());
    assertEquals(Boolean.TRUE, url.getJSON("properties").get(PropertyType.ESCAPE_XML.getClass().getName()));
  }

  @Test
  public void testInvalidProperty() throws Exception {
    MockApplication<?> app = application("plugin.controller.url.invalidproperty");
    app.init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }
}
