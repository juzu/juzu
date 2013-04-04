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

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderTestCase extends AbstractInjectTestCase {

  private static final Pattern P = Pattern.compile("([0-9]+)\\[(.*)\\]");

  public RenderTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testIndex() throws Exception {
    MockApplication<?> app = application("plugin.controller.render.index").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("index", render.assertStringResult());
  }

  @Test
  public void testParameterizedIndex() throws Exception {
    MockApplication<?> app = application("plugin.controller.render.parameterizedindex").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    Matcher m = P.matcher(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("0", m.group(1));
    render = (MockRenderBridge)client.invoke(m.group(2));
    m.reset(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("1", m.group(1));
    render = (MockRenderBridge)client.invoke(m.group(2));
    m.reset(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("0", m.group(1));
  }

  @Test
  public void testOverridenIndex() throws Exception {
    MockApplication<?> app = application("plugin.controller.render.overridenindex").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    Matcher m = P.matcher(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("0", m.group(1));
    render = (MockRenderBridge)client.invoke(m.group(2));
    m.reset(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("1", m.group(1));
    render = (MockRenderBridge)client.invoke(m.group(2));
    m.reset(render.assertStringResult());
    assertTrue("Was expecting " + render.assertStringResult() + " to match", m.matches());
    assertEquals("2", m.group(1));
  }

  @Test
  public void testResponse() throws Exception {
    MockApplication<?> app = application("plugin.controller.render.response").init();

    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("foo", render.assertStringResult());
  }

  @Test
  public void testUpdate() throws Exception {
    MockApplication<?> app = application("plugin.controller.render.update").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    String url = render.assertStringResult();
    MockActionBridge action = (MockActionBridge)client.invoke(url);
    action.assertRender("A.done", Collections.<String, String>emptyMap());

  }
}
