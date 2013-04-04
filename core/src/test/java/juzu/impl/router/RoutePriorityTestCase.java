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

import org.junit.Test;

import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RoutePriorityTestCase extends AbstractControllerTestCase {

  @Test
  public void testExactMatchingAfterWildcard() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{foo}");
    Route b = router.append("/foo");

    router.assertRoute(a, "/foo");
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/b", a.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "b")).render());
  }

  @Test
  public void testExactMatchingBeforeWildcard() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/foo");
    Route b = router.append("/{foo}");

    router.assertRoute(a, "/foo");
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/b", b.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "foo")).render());
  }
}
