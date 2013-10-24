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
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PreDestroyTestCase extends AbstractInjectTestCase {

  public PreDestroyTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testRenderPhase() throws Exception {
    if (getDI() == InjectorProvider.WELD) {
      MockApplication<?> app = application("plugin.controller.predestroy").init();

      //
      MockClient client = app.client();
      client.render();
      Integer count = Registry.get("count");
      assertEquals((Integer)1, count);
    }
  }
}
