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
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import juzu.test.protocol.mock.MockResourceBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceTestCase extends AbstractInjectTestCase {

  public ResourceTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testNotFound() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "resource", "notfound").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    MockResourceBridge resource = (MockResourceBridge)client.invoke(render.assertStringResult());
    resource.assertNotFound();
  }

  @Test
  public void testBinary() throws Exception {
    MockApplication<?> app = application("plugin", "controller", "resource", "binary").init();

    //
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    MockResourceBridge resource = (MockResourceBridge)client.invoke(render.assertStringResult());
    assertEquals("hello", new String(resource.assertBinaryResult(), "UTF-8"));
    assertEquals("application/octet-stream", resource.getMimeType());
  }
}
