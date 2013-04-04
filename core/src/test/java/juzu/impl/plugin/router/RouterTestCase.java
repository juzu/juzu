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

package juzu.impl.plugin.router;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterTestCase extends AbstractInjectTestCase {

  public RouterTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testDeclaration() throws Exception {
    MockApplication<?> application = application("plugin.router.declaration").init();

    //
    ApplicationDescriptor descriptor = application.getContext().getDescriptor();
    RouteDescriptor desc = new RouteDescriptor(descriptor.getConfig().getJSON("router"));
    List<RouteDescriptor> routes = desc.getChildren();
    assertEquals(3, routes.size());
  }
}
