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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RenderTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot1() throws Exception {
    Router router = new Router();
    Route r = router.append("/");

    //
    assertEquals("/", r.matches(Collections.<String, String>emptyMap()).render());
  }

  @Test
  public void testRoot2() throws Exception {
    Router router = new Router();
    Route r = router.append("");

    //
    assertEquals("/", r.matches(Collections.<String, String>emptyMap()).render());
  }

  @Test
  public void testA() throws Exception {
    Router router = new Router();
    Route r = router.append("/a");

    //
    assertEquals("/a", r.matches(Collections.<String, String>emptyMap()).render());
  }

  @Test
  public void testAB() throws Exception {
    Router router = new Router();
    Route r = router.append("/a/b");

    //
    assertEquals("/a/b", r.matches(Collections.<String, String>emptyMap()).render());
  }

  @Test
  public void testPathParam() throws Exception {
    Router router = new Router();
    Route r = router.append("/{p}");

    //
    assertEquals("/a", r.matches(Collections.singletonMap(Names.P, "a")).render());
    assertNull(r.matches(Collections.<String, String>emptyMap()));
  }

  @Test
  public void testSimplePatternPathParam() throws Exception {
    Router router = new Router();
    Route r = router.append("/{p}", Collections.singletonMap(Names.P, PathParam.matching("a")));

    //
    assertEquals("/a", r.matches(Collections.singletonMap(Names.P, "a")).render());
    assertNull(r.matches(Collections.singletonMap(Names.P, "ab")));
  }

  @Test
  public void testPrecedence() throws Exception {
    Router router = new Router();
    Route a = router.append("/a");
    Route b = router.append("/{p}/b", Collections.singletonMap(Names.P, PathParam.matching("a")));

    //
    assertEquals("/a", a.matches(Collections.<String, String>emptyMap()).render());
    assertEquals("/a/b", b.matches(Collections.singletonMap(Names.P, "a")).render());
  }

  @Test
  public void testLang() throws Exception {
    Router router = new Router();
    Route r = router.append("/{a}b", Collections.singletonMap(Names.A, PathParam.matching("(([A-Za-z]{2})/)?").preservePath(true)));

    //
    assertEquals("/fr/b", r.matches(Collections.singletonMap(Names.A, "fr/")).render());
    assertEquals("/b", r.matches(Collections.singletonMap(Names.A, "")).render());
  }

  @Test
  public void testDisjunction() throws Exception {
    Router router = new Router();
    Route r = router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching("a|b")));

    //
    assertEquals("/b", r.matches(Collections.singletonMap(Names.A, "b")).render());
  }

  @Test
  public void testCaptureGroup() throws Exception {
    Router router = new Router();
    Route r = router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching("a(.)c").captureGroup(true)));

    //
    assertEquals("/abc", r.matches(Collections.singletonMap(Names.A, "b")).render());
  }
}
