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

package juzu.plugin.jackson;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.lang.reflect.Field;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JacksonRequestObjectTestCase extends AbstractJacksonRequestTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.jackson.request.object");
  }

  @Override
  public void testRequest() throws Exception {
    super.testRequest();
    assertNotNull(payload);
    Class<?> payloadType = payload.getClass();
    assertEquals("plugin.jackson.request.object.Foo", payloadType.getName());
    Field fooField = payloadType.getDeclaredField("foo");
    assertEquals("bar", fooField.get(payload));
  }
}
