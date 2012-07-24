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

package juzu.templating.mustache;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MustacheTestCase extends AbstractInjectTestCase {

  public MustacheTestCase(InjectImplementation di) {
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
    if (getDI() != InjectImplementation.INJECT_GUICE) {
      MockApplication<?> app = application("parameterdeclaration").init();
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("bar", render.assertStringResult());
    }
  }
}
