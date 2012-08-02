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

import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockActionBridge;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodParametersTestCase extends AbstractInjectTestCase {

  public MethodParametersTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testStringArray() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "method", "parameters", "string", "array").init();

    //
    MockClient client = app.client();

    //
    MockRenderBridge render = client.render("none");
    MockRenderBridge mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals("", mv.assertStringResult());

    //
    render = client.render("0");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals("", mv.assertStringResult());

    //
    render = client.render("1");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals(Arrays.asList("bar").toString(), mv.assertStringResult());

    //
    render = client.render("2");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals(Arrays.asList("bar_1", "bar_2").toString(), mv.assertStringResult());
  }

  @Test
  public void testStringList() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "method", "parameters", "string", "list").init();

    //
    MockClient client = app.client();

    //
    MockRenderBridge render = client.render("none");
    MockRenderBridge mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals("", mv.assertStringResult());

    //
    render = client.render("0");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals("", mv.assertStringResult());

    //
    render = client.render("1");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals(Arrays.asList("bar").toString(), mv.assertStringResult());

    //
    render = client.render("2");
    mv = (MockRenderBridge)client.invoke(render.assertStringResult());
    assertEquals(Arrays.asList("bar_1", "bar_2").toString(), mv.assertStringResult());
  }

  @Test
  public void testBean() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "method", "parameters", "bean").init();

    //
    MockClient client = app.client();

    //
    assertBean(client, "a", "v");
    assertBean(client, "b", Arrays.asList("v1", "v2").toString());
    assertBean(client, "c", Arrays.asList("v1", "v2").toString());
    assertBean(client, "d", "v");
    assertBean(client, "e", Arrays.asList("v1", "v2").toString());
    assertBean(client, "f", Arrays.asList("v1", "v2").toString());
    assertBean(client, "g", "s_valuev");
    assertBean(client, "g", "s_valuev");
    assertBean(client, "h", "s_valuev");

  }

  private void assertBean(MockClient client, String name, Object expected) throws Exception {
    MockRenderBridge render = client.render(name);
    String url = render.assertStringResult();
    MockRenderBridge m = (MockRenderBridge)client.invoke(url);
    assertEquals(expected, m.assertStringResult());

    render = client.render(name + "Action");
    url = render.assertStringResult();
    MockActionBridge action = (MockActionBridge)client.invoke(url);
    MockRenderBridge m2 = (MockRenderBridge)client.invoke(action.assertUpdate());
    assertEquals(expected, m2.assertStringResult());
  }
}
