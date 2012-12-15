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

import junit.framework.Assert;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ContextualArgumentTestCase extends AbstractTestCase {

  public ContextualArgumentTestCase() {
  }

  @Test
  public void testFoo() throws Exception {
    // We only use CDI for its capability to discover beans automatically
    MockApplication<?> application = application(InjectorProvider.CDI_WELD, "plugin.controller.contextual");
    application.init();
    MockClient client = application.client();
    MockRenderBridge request = client.render();
    Assert.assertEquals("__foo__", request.assertStringResult());
  }
}

