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

import junit.framework.Assert;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ContextualArgumentTestCase extends AbstractTestCase {

  public ContextualArgumentTestCase() {
  }

  @Test
  public void testSimple() throws Exception {
    // We only use CDI for its capability to discover beans automatically
    MockApplication<?> application = application(InjectorProvider.CDI_WELD, "plugin.controller.contextual.simple");
    application.init();
    MockClient client = application.client();
    MockViewBridge request = client.render();
    Assert.assertEquals("__foo__", request.assertStringResult());
  }

  @Test
  public void testRequest() throws Exception {
    MockApplication<?> application = application(InjectorProvider.INJECT_GUICE, "plugin.controller.contextual.request");
    application.init();
    MockClient client = application.client();
    MockViewBridge request = client.render();
    Assert.assertEquals("pass", request.assertStringResult());
  }
}

