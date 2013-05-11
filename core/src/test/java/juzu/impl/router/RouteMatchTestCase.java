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

package juzu.impl.router;

import juzu.impl.common.UriBuilder;
import juzu.io.UndeclaredIOException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMatchTestCase extends AbstractControllerTestCase {

  private void assertMatch(String expected, Route route, Map<String, String> parameters) {
    RouteMatch match = route.matches(parameters);
    assertNotNull("Was expecting to match " + expected, match);
    StringBuilder sb = new StringBuilder();
    UriBuilder writer = new UriBuilder(sb);
    try {
      match.render(writer);
      for (Map.Entry<String, String> entry : match.getUnmatched().entrySet()) {
        writer.appendQueryParameter(entry.getKey(), entry.getValue());
      }
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  @Test
  public void testPath() {
    Router router = new Router();
    Route a = router.append("/a");
    RouteMatch match = a.matches(Collections.<String, String>emptyMap());
    assertNotNull(match);
    assertMatch("/a?a=foo", a, Collections.<String, String>singletonMap(Names.A, "foo"));
  }

  @Test
  public void testPathParam() {
    Router router = new Router();
    Route a = router.append("/{a}");
    assertNull(a.matches(Collections.<String, String>emptyMap()));
    assertMatch("/foo", a, Collections.<String, String>singletonMap(Names.A, "foo"));
  }
}
