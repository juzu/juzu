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

package juzu.impl.router;

import juzu.impl.common.QualifiedName;
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
    router.append("/");

    //
    assertNull(router.route(""));
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testRoot2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("");

    //
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "");
    assertNull(router.route("/"));
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testRoot3() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/", RouteKind.MATCH_ANY);

    //
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testA1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a");

    //
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a");
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
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/");
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
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/");
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
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/b");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b");
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
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b/");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/b/");
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
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/b");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b/");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a/b/");
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
    router.append("/{<a>p}");

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
    router.append("/{<a>p}/b");

    //
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a");
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
    router.append("/{<(([A-Za-z]{2})/)?>[p]a}b");

    //
    router.assertRoute(Collections.singletonMap(Names.A, "fr/"), "/fr/b");
    router.assertRoute(Collections.singletonMap(Names.A, ""), "/b");
  }

  @Test
  public void testOptionalParameter() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{<a?>[p]a}/b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
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
    Route a = router.append("/{<a?>[p]a}/ab/c");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "");
    router.assertRoute(a, expectedParameters, "/ab/c");
    assertEquals("/ab/c", a.matches(expectedParameters).render());
  }

  @Test
  public void testPartialMatching() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<abc>[p]a}");

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
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
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
    Route a = router.append("/{<a?>[p]a}");
    Route b = a.append("/b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
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
    router.append("/{<a|b>a}{b}");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
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
    router.append("/{<a(.)c>[c]a}");

    //
    router.assertRoute(Collections.singletonMap(Names.A, "b"), "/abc");
  }

  @Test
  public void testPreservePath() throws Exception {

    RouterAssert router = new RouterAssert();
    router.append("/{<.*>[p]a}");

    router.assertRoute(Collections.singletonMap(Names.A, "a"), "/a");
    router.assertRoute(Collections.singletonMap(Names.A, "//"), "///");
    router.assertRoute(Collections.singletonMap(Names.A, "a/"), "/a/");
  }
}
