/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatchTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot() throws Exception {
    Router router = new Router();
    router.append("/");

    //
    assertNull(router.route(""));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/"));
    assertNull(router.route("/a"));
    assertNull(router.route("a"));
  }

  @Test
  public void testA() throws Exception {
    Router router = new Router();
    router.append("/a");

    //
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a"));
    assertNull(router.route("a"));
    assertNull(router.route("a/"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/"));
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b"));
  }

  @Test
  public void testAB() throws Exception {
    Router router = new Router();
    router.append("/a/b");

    //
    assertNull(router.route("a/b"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b/"));
    assertNull(router.route("a/b/"));
    assertNull(router.route(""));
    assertNull(router.route("/"));
    assertNull(router.route("/b"));
    assertNull(router.route("b"));
    assertNull(router.route("/a/b/c"));
  }

  @Test
  public void testParameter() throws Exception {
    Router router = new Router();
    router.append("/{p}");

    //
    assertEquals(Collections.singletonMap(Names.P, "a"), router.route("/a"));
  }

  @Test
  public void testParameterPropagationToDescendants() throws Exception {
    Router router = new Router();
    router.append("/a").addParam("p", "a").append("/b");

    //
    assertEquals(Collections.singletonMap(Names.P, "a"), router.route("/a/b"));
  }

  @Test
  public void testSimplePattern() throws Exception {
    Router router = new Router();
    router.append("/{<a>p}");

    //
    assertEquals(Collections.singletonMap(Names.P, "a"), router.route("/a"));
    assertNull(router.route("a"));
    assertNull(router.route("/ab"));
    assertNull(router.route("ab"));
  }

  @Test
  public void testPrecedence() throws Exception {
    Router router = new Router();
    router.append("/a");
    router.append("/{<a>p}/b");

    //
    assertNull(router.route("a"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/"));
    assertEquals(Collections.singletonMap(Names.P, "a"), router.route("/a/b"));
  }

  @Test
  public void testTwoRules1() throws Exception {
    Router router = new Router();
    router.append("/a").addParam("b", "b");
    router.append("/a/b");

    //
    assertEquals(Collections.singletonMap(Names.B, "b"), router.route("/a"));
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b"));
  }

  @Test
  public void testTwoRules2() throws Exception {
    Router router = new Router();
    router.append("/{a}").addParam("b", "b");
    router.append("/{a}/b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    assertEquals(expectedParameters, router.route("/a"));
    assertEquals(Collections.singletonMap(Names.A, "a"), router.route("/a/b"));
  }

  @Test
  public void testLang() throws Exception {
    Router router = new Router();
    router.append("/{<(([A-Za-z]{2})/)?>[p]a}b");

    //
    assertEquals(Collections.singletonMap(Names.A, "fr/"), router.route("/fr/b"));
    assertEquals(Collections.singletonMap(Names.A, ""), router.route("/b"));
  }

  @Test
  public void testOptionalParameter() throws Exception {
    Router router = new Router();
    router.append("/{<a?>[p]a}/b").addParam("b", "b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    assertEquals(expectedParameters, router.route("/a/b"));
    assertEquals("/a/b", router.render(expectedParameters));

    //
    expectedParameters.put(Names.A, "");
    assertEquals(expectedParameters, router.route("/b"));
    assertEquals("/b", router.render(expectedParameters));
  }

  @Test
  public void testAvoidMatchingPrefix() throws Exception {
    Router router = new Router();
    router.append("/{<a?>[p]a}/ab/c");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "");
    assertEquals(expectedParameters, router.route("/ab/c"));
    assertEquals("/ab/c", router.render(expectedParameters));
  }

  @Test
  public void testPartialMatching() throws Exception {
    Router router = new Router();
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
    Router router = new Router();
    router.append("/{<a?>[p]a}").append("/b").addParam("b", "b");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "b");
    assertEquals(expectedParameters, router.route("/a/b"));
    assertEquals("/a/b", router.render(expectedParameters));

    //
    expectedParameters.put(Names.A, "");
    assertEquals(expectedParameters, router.route("/b"));
    assertEquals("/b", router.render(expectedParameters));
  }

  @Test
  public void testMatcher() throws Exception {
    Router router = new Router();
    router.append("/{a}");
    router.append("/a").addParam("b", "b_value");

    Iterator<Map<QualifiedName, String>> i = router.root.route("/a", Collections.<String, String[]>emptyMap());
    Map<QualifiedName, String> s1 = i.next();
    assertEquals(Collections.singletonMap(Names.A, "a"), s1);
    Map<QualifiedName, String> s2 = i.next();
    assertEquals(Collections.singletonMap(Names.B, "b_value"), s2);
    assertFalse(i.hasNext());
  }

  @Test
  public void testDisjunction() throws Exception {
    Router router = new Router();
    router.append("/{<a|b>a}{b}");

    //
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.A, "a");
    expectedParameters.put(Names.B, "c");

    //
    Iterator<Map<QualifiedName, String>> i = router.root.route("/ac", Collections.<String, String[]>emptyMap());
    Map<QualifiedName, String> s1 = i.next();
    assertEquals(expectedParameters, s1);
    assertFalse(i.hasNext());

    i = router.root.route("/bc", Collections.<String, String[]>emptyMap());
    s1 = i.next();
    expectedParameters.put(Names.A, "b");
    assertEquals(expectedParameters, s1);
    assertFalse(i.hasNext());
  }

  @Test
  public void testCaptureGroup() throws Exception {
    Router router = new Router();
    router.append("/{<a(.)c>[c]a}");

    //
    Iterator<Map<QualifiedName, String>> i = router.root.route("/abc", Collections.<String, String[]>emptyMap());
    Map<QualifiedName, String> s1 = i.next();
    assertEquals(Collections.singletonMap(Names.A, "b"), s1);
    assertFalse(i.hasNext());
  }
}
