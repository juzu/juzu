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

import juzu.impl.common.Builder;
import juzu.impl.common.QualifiedName;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatchTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/");

    //
    assertNull(router.route(""));
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/");
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testA() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a");

    //
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a");
    assertNull(router.route("a"));
    assertNull(router.route("a/"));
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/");
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b"));
  }

  @Test
  public void testAB() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a/b");

    //
    assertNull(router.route("a/b"));
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b/");
    assertNull(router.route("a/b/"));
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
    router.append("/a").addParam("p", "a").append("/b");

    //
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a/b");
  }

  @Test
  public void testSimplePattern() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<a>p}");

    //
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a");
    assertNull(router.route("a"));
    assertNull(router.route("/ab"));
    assertNull(router.route("ab"));
  }

  @Test
  public void testPrecedence() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a");
    router.append("/{<a>p}/b");

    //
    assertNull(router.route("a"));
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/");
    router.assertRoute(Collections.singletonMap(Names.P, "a"), "/a/b");
  }

  @Test
  public void testTwoRules1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/a").addParam("b", "b");
    router.append("/a/b");

    //
    router.assertRoute(Collections.singletonMap(Names.B, "b"), "/a");
    router.assertRoute(Collections.<QualifiedName, String>emptyMap(), "/a/b");
  }

  @Test
  public void testTwoRules2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}").addParam("b", "b");
    router.append("/{a}/b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    router.assertRoute(expectedParameters, "/a");
    router.assertRoute(Collections.singletonMap(Names.A, "a"), "/a/b");
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
    router.append("/{<a?>[p]a}/b").addParam("b", "b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    router.assertRoute(expectedParameters, "/a/b");
    assertEquals("/a/b", router.render(expectedParameters));

    //
    expectedParameters.put(Names.A, "");
    router.assertRoute(expectedParameters, "/b");
    assertEquals("/b", router.render(expectedParameters));
  }

  @Test
  public void testAvoidMatchingPrefix() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<a?>[p]a}/ab/c");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "");
    router.assertRoute(expectedParameters, "/ab/c");
    assertEquals("/ab/c", router.render(expectedParameters));
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
    router.append("/{<a?>[p]a}").append("/b").addParam("b", "b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    router.assertRoute(expectedParameters, "/a/b");
    assertEquals("/a/b", router.render(expectedParameters));

    //
    expectedParameters.put(Names.A, "");
    router.assertRoute(expectedParameters, "/b");
    assertEquals("/b", router.render(expectedParameters));
  }

  @Test
  public void testMatcher() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{a}");
    router.append("/a").addParam("b", "b_value");

    //
    router.assertRoutes(Builder.list(Collections.singletonMap(Names.A, "a")).add(Collections.singletonMap(Names.B, "b_value")).build(), "/a");
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
}
