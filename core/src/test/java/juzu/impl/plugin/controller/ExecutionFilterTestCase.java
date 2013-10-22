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

import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.request.RequestFilter;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ExecutionFilterTestCase extends AbstractInjectTestCase {

  public ExecutionFilterTestCase(InjectorProvider di) {
    super(di);
  }

  /** . */
  public static final LinkedList<String> events = new LinkedList<String>();

  @Test
  public void testLifeCycle() throws Exception {
    events.clear();
    MockApplication<?> app = application("plugin.controller.executionfilter.lifecycle").init();
    Tools.list(app.getLifeCycle().resolveBeans(RequestFilter.class));
    MockClient client = app.client();
    MockViewBridge render = client.render();
    assertEquals((Object)Arrays.asList("execute", "onCommand", "beforeRun", "run", "afterRun", "hello"), events);
  }

}
