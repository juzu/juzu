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
import juzu.test.Registry;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.util.ConcurrentModificationException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleTestCase extends AbstractInjectTestCase {

  public LifeCycleTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testDispatch() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.dispatch").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertStringResult("index");
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }

  @Test
  public void testOverrideBegin() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.overridebegin").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertStringResult("begin");
    Integer count = Registry.get("count");
    assertEquals((Integer)0, count);
  }

  @Test
  public void testOverrideEnd() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.overrideend").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertStringResult("end");
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }

  @Test
  public void testFailureBegin() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failurebegin").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)0, count);
  }

  @Test
  public void testFailureDispatch() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failuredispatch").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)1, count);
  }

  @Test
  public void testFailureEnd() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failureend").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }
}
