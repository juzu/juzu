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
import juzu.test.Registry;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FilterTestCase extends AbstractInjectTestCase {

  public FilterTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testLifeCycle() throws Exception {
    Registry.unset("request.filter.lifecycle");

    MockApplication<?> app = application("plugin", "controller", "filter", "lifecycle").init();

    //
    app.getContext().getLifecycles();
    assertEquals("created", Registry.get("request.filter.lifecycle"));

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("after", Registry.get("request.filter.lifecycle"));
  }

  @Test
  public void testFailure() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "filter", "failure").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("pass", render.assertStringResult());
  }
}
