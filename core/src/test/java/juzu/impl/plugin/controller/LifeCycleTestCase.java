/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.plugin.controller;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.Registry;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
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
    MockRenderBridge render = client.render();
    render.assertStringResult("index");
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }

  @Test
  public void testOverrideBegin() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.overridebegin").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    render.assertStringResult("begin");
    Integer count = Registry.get("count");
    assertEquals((Integer)0, count);
  }

  @Test
  public void testOverrideEnd() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.overrideend").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    render.assertStringResult("end");
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }

  @Test
  public void testFailureBegin() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failurebegin").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)0, count);
  }

  @Test
  public void testFailureDispatch() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failuredispatch").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }

  @Test
  public void testFailureEnd() throws Exception {
    MockApplication<?> app = application("plugin.controller.lifecycle.failureend").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    render.assertFailure(ConcurrentModificationException.class);
    Integer count = Registry.get("count");
    assertEquals((Integer)2, count);
  }
}
