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
import juzu.test.protocol.mock.MockRequestBridge;
import org.junit.Test;

import javax.naming.AuthenticationException;
import java.util.ConcurrentModificationException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ThrowableTestCase extends AbstractInjectTestCase {
  public ThrowableTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testConstructorChecked() throws Exception {
    MockApplication<?> app = application("plugin.controller.constructor.throwable.checked").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(AuthenticationException.class);
  }

  @Test
  public void testConstructorRuntime() throws Exception {
    MockApplication<?> app = application("plugin.controller.constructor.throwable.runtime").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
  }

  @Test
  public void testConstructorError() throws Exception {
    MockApplication<?> app = application("plugin.controller.constructor.throwable.error").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(UnknownError.class);
  }

  @Test
  public void testRenderChecked() throws Exception {
    MockApplication<?> app = application("plugin.controller.view.throwable.checked").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(AuthenticationException.class);
  }

  @Test
  public void testRenderRuntime() throws Exception {
    MockApplication<?> app = application("plugin.controller.view.throwable.runtime").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
  }

  @Test
  public void testRenderError() throws Exception {
    MockApplication<?> app = application("plugin.controller.view.throwable.error").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    render.assertFailure(UnknownError.class);
  }

  @Test
  public void testActionChecked() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.throwable.checked").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(AuthenticationException.class);
  }

  @Test
  public void testActionRuntime() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.throwable.runtime").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(ConcurrentModificationException.class);
  }

  @Test
  public void testActionError() throws Exception {
    MockApplication<?> app = application("plugin.controller.action.throwable.error").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(UnknownError.class);
  }

  @Test
  public void testResourceChecked() throws Exception {
    MockApplication<?> app = application("plugin.controller.resource.throwable.checked").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(AuthenticationException.class);
  }

  @Test
  public void testResourceRuntime() throws Exception {
    MockApplication<?> app = application("plugin.controller.resource.throwable.runtime").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(ConcurrentModificationException.class);
  }

  @Test
  public void testResourceError() throws Exception {
    MockApplication<?> app = application("plugin.controller.resource.throwable.error").init();

    //
    MockClient client = app.client();
    MockViewBridge render = client.render();
    MockRequestBridge request = client.invoke(render.assertStringResult());
    request.assertFailure(UnknownError.class);
  }
}
