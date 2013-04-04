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

package juzu.templating.mustache;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MustacheTestCase extends AbstractInjectTestCase {

  public MustacheTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testBasic() throws Exception {
    MockApplication<?> app = application("basic").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("foo", render.assertStringResult());
  }

  @Test
  public void testResolveParameter() throws Exception {
    MockApplication<?> app = application("resolveparameter").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("bar", render.assertStringResult());
  }

  @Test
  public void testResolveBean() throws Exception {
    MockApplication<?> app = application("resolvebean").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("bar", render.assertStringResult());
  }

  @Test
  public void testPartial() throws Exception {
    MockApplication<?> app = application("partial").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("<bar>foo</bar>", render.assertStringResult());
  }

  @Test
  public void testParameterDeclaration() throws Exception {
    if (getDI() != InjectorProvider.INJECT_GUICE) {
      MockApplication<?> app = application("parameterdeclaration").init();
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("bar", render.assertStringResult());
    }
  }
}
