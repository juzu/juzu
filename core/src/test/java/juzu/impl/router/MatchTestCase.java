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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatchTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot1() throws Exception {
    RouterAssert router = new RouterAssert();
    Route foo = router.append("/");
    assertNotSame(foo, router);

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "");
    router.assertRoute(Collections.<String, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testRoot2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("");

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "");
    router.assertRoute(Collections.<String, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testRoot3() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/", RouteKind.MATCH_ANY);

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "");
    router.assertRoute(Collections.<String, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testEmpty1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/").append("/foo");

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "");
    router.assertRoute(Collections.<String, String>emptyMap(), "/");
    router.assertRoute(Collections.<String, String>emptyMap(), "/foo");
    router.assertRoute(Collections.<String, String>emptyMap(), "foo");
  }

  @Test
  public void testEmpty2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/foo").append("/").append("/bar");

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "/foo");
    router.assertRoute(Collections.<String, String>emptyMap(), "foo");
    router.assertRoute(Collections.<String, String>emptyMap(), "foo/bar");
    router.assertRoute(Collections.<String, String>emptyMap(), "/foo/bar");
  }

  @Test
  public void testA1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a");

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "/a");
    router.assertRoute(Collections.<String, String>emptyMap(), "a");
    assertNull(router.route("a/"));
    assertNull(router.route("/a/"));
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b"));
  }

  @Test
  public void testA2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a/");

    //
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
    router.assertRoute(Collections.<String, String>emptyMap(), "a/");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/");
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b"));
  }

  @Test
  public void testA3() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a", RouteKind.MATCH_ANY);

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "/a");
    router.assertRoute(Collections.<String, String>emptyMap(), "a");
    router.assertRoute(Collections.<String, String>emptyMap(), "a/");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/");
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b"));
  }

  @Test
  public void testAB1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a/b");

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "a/b");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/b");
    assertNull(router.route("/a/b/"));
    assertNull(router.route("a/b/"));
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b/c"));
  }

  @Test
  public void testAB2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a/b/");

    //
    assertNull(router.route("a/b"));
    assertNull(router.route("/a/b"));
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/b/");
    router.assertRoute(Collections.<String, String>emptyMap(), "a/b/");
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b/c"));
  }

  @Test
  public void testAB3() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a/b", RouteKind.MATCH_ANY);

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "a/b");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/b");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a/b/");
    router.assertRoute(Collections.<String, String>emptyMap(), "a/b/");
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b/c"));
  }

  @Test
  public void testParameter() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{p}");

    //
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a");
  }

  @Test
  public void testParameterPropagationToDescendants() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/a").append("/b");

    //
    router.assertRoute(a, "/a/b");
  }

  @Test
  public void testSimplePattern() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{p}", Collections.singletonMap(Names.P, PathParam.matching("a")));

    //
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a");
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "a");
    assertNull(router.route("/ab"));
    assertNull(router.route("ab"));
  }

  @Test
  public void testPrecedence() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a");
    router.append("/{p}/b", Collections.singletonMap(Names.P, PathParam.matching("a")));

    //
    router.assertRoute(Collections.<String, String>emptyMap(), "a");
    router.assertRoute(Collections.<String, String>emptyMap(), "/a");
    assertNull(router.route("/a/"));
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a/b");
  }

  @Test
  public void testTwoRules1() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/a");
    Route b = router.append("/a/b");

    //
    router.assertRoute(a, "/a");
    router.assertRoute(b, "/a/b");
  }

  @Test
  public void testTwoRules2() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{a}");
    Route b = router.append("/{a}/b");

    //
    router.assertRoute(a, Collections.singletonMap(Names.A, "a"), "/a");
    router.assertRoute(b, Collections.singletonMap(Names.A, "a"), "/a/b");
  }

  @Test
  public void testLang() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}b", Collections.singletonMap(Names.A, PathParam.matching("(([A-Za-z]{2})/)?").preservePath(true)));

    //
    router.assertRoute(Collections.singletonMap(Names.A, "fr/"), "/fr/b");
    router.assertRoute(Collections.singletonMap(Names.A, ""), "/b");
  }

  @Test
  public void testOptionalParameter() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{a}/b", Collections.singletonMap(Names.A, PathParam.matching("a?").preservePath(true)));

    //
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.A, "a");
    router.assertRoute(a, Collections.singletonMap(Names.A, "a"), "/a/b");
    assertEquals("/a/b", a.matches(expectedParameters).render());

    //
    expectedParameters.put(Names.A, "");
    router.assertRoute(expectedParameters, "/b");
    assertEquals("/b", a.matches(expectedParameters).render());
  }

  @Test
  public void testAvoidMatchingPrefix() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{a}/ab/c", Collections.singletonMap(Names.A, PathParam.matching("a?").preservePath(true)));

    //
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.A, "");
    router.assertRoute(a, expectedParameters, "/ab/c");
    assertEquals("/ab/c", a.matches(expectedParameters).render());
  }

  @Test
  public void testPartialMatching() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching("abc").preservePath(true)));

    //
    assertNull(router.route("/abcdef"));
  }

/*
   public void testLookAhead() throws Exception
   {
      Router router = router().
         add(route("/{a}").
            with(
               pathParam("a").matchedBy("(.(?=/))?").preservingPath()).
            sub(route("/{b}").
               with(pathParam("b").matchedBy(".").preservingPath()))
            ).build();

      //
      Map<String, String> expectedParameters = new HashMap<String, String>();
      expectedParameters.put(Names.A, "");
      expectedParameters.put(Names.B, "b");
      assertEquals(expectedParameters, router.route("/b"));
      assertEquals("/b", router.render(expectedParameters));

      //
      expectedParameters.put(Names.A, "a");
      assertEquals(expectedParameters, router.route("/a/b"));
      assertEquals("/a/b", router.render(expectedParameters));
   }
*/

  @Test
  public void testZeroOrOneFollowedBySubRoute() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching("a?").preservePath(true)));
    Route b = a.append("/b");

    //
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.A, "a");
    router.assertRoute(expectedParameters, "/a/b");
    RouteMatch resolve = b.matches(expectedParameters);
    assertNotNull(resolve);
    assertEquals("/a/b", resolve.render());

    //
    router.assertRoute(b, "/b");
  }

  @Test
  public void testMatcher() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{a}");
    Route b = router.append("/a");

    //
    router.assertRoutes(Arrays.asList(a, b), "/a");
  }

  @Test
  public void testDisjunction() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}{b}", Collections.singletonMap(Names.A, PathParam.matching("a|b")));

    //
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "c");

    //
    router.assertRoute(expectedParameters, "/ac");
    expectedParameters.put(Names.A, "b");
    router.assertRoute(expectedParameters, "/bc");
  }

  @Test
  public void testCaptureGroup() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching("a(.)c").captureGroup(true)));

    //
    router.assertRoute(Collections.singletonMap(Names.A, "b"), "/abc");
  }

  @Test
  public void testPreservePath() throws Exception {

    RouterAssert router = new RouterAssert();
    router.append("/{a}", Collections.singletonMap(Names.A, PathParam.matching(".*").preservePath(true)));

    router.assertRoute(Collections.singletonMap(Names.A, "a"), "/a");
    router.assertRoute(Collections.singletonMap(Names.A, "//"), "///");
    router.assertRoute(Collections.singletonMap(Names.A, "a/"), "/a/");
  }
}
