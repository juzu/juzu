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

import juzu.impl.utils.QualifiedName;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RequestParamTestCase extends AbstractControllerTestCase {

  @Test
  public void testRoot() throws Exception {
    Router router = new Router();
    router.append("?a={<a>[r]foo}");

    //
    assertNull(router.route("/"));
    assertEquals(Collections.singletonMap(Names.FOO, "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));

    //
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
    URIHelper renderContext = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "a"), renderContext.writer);
    assertEquals("/", renderContext.getPath());
    assertMapEquals(Collections.singletonMap("a", new String[]{"a"}), renderContext.getQueryParams());
  }

  @Test
  public void testSegment() throws Exception {
    Router router = new Router();
    router.append("/a?a={<a>[r]foo}");

    //
    assertNull(router.route("/a"));
    assertEquals(Collections.singletonMap(Names.FOO, "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));

    //
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
    URIHelper renderContext = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "a"), renderContext.writer);
    assertEquals("/a", renderContext.getPath());
    assertMapEquals(Collections.singletonMap("a", new String[]{"a"}), renderContext.getQueryParams());
  }

  @Test
  public void testValuePattern() throws Exception {
    Router router = new Router();
    router.append("/a?a={<[0-9]+>[r]foo}");

    //
    assertNull(router.route("/a"));
    assertNull(router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
    assertEquals(Collections.singletonMap(Names.FOO, "0123"), router.route("/a", Collections.singletonMap("a", new String[]{"0123"})));

    //
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
    assertEquals("", router.render(Collections.singletonMap(Names.FOO, "a")));
    URIHelper renderContext = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "12"), renderContext.writer);
    assertEquals("/a", renderContext.getPath());
    assertMapEquals(Collections.singletonMap("a", new String[]{"12"}), renderContext.getQueryParams());
  }

  @Test
  public void testPrecedence() throws Exception {
    Router router = new Router();
    router.append("/a?a={<a>[r]foo}");
    router.append("/a?b={<b>[r]bar}");

    //
    assertNull(router.route("/a"));
    assertEquals(Collections.singletonMap(Names.FOO, "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
    assertEquals(Collections.singletonMap(Names.BAR, "b"), router.route("/a", Collections.singletonMap("b", new String[]{"b"})));

    //
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
    URIHelper renderContext1 = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "a"), renderContext1.writer);
    assertEquals("/a", renderContext1.getPath());
    assertMapEquals(Collections.singletonMap("a", new String[]{"a"}), renderContext1.getQueryParams());
    URIHelper renderContext2 = new URIHelper();
    router.render(Collections.singletonMap(Names.BAR, "b"), renderContext2.writer);
    assertEquals("/a", renderContext2.getPath());
    assertMapEquals(Collections.singletonMap("b", new String[]{"b"}), renderContext2.getQueryParams());
  }

  @Test
  public void testInheritance() throws Exception {
    Router router = new Router();
    router.append("/a?a={<a>[r]foo}").append("/b?b={<b>[r]bar}");

    //
    assertNull(router.route("/a"));
    // assertEquals(Collections.singletonMap(Names.FOO, "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
    assertNull(router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
    assertNull(router.route("/a/b"));
    Map<String, String[]> requestParameters = new HashMap<String, String[]>();
    requestParameters.put("a", new String[]{"a"});
    requestParameters.put("b", new String[]{"b"});
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.FOO, "a");
    expectedParameters.put(Names.BAR, "b");
    assertEquals(expectedParameters, router.route("/a/b", requestParameters));

    //
    assertEquals("", router.render(Collections.<QualifiedName, String>emptyMap()));
    URIHelper renderContext1 = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "a"), renderContext1.writer);
    // assertEquals("/a", renderContext1.getPath());
    // assertEquals(Collections.singletonMap("a", "a"), renderContext1.getQueryParams());
    assertEquals("", renderContext1.getPath());
    URIHelper renderContext2 = new URIHelper();
    router.render(expectedParameters, renderContext2.writer);
    assertEquals("/a/b", renderContext2.getPath());
    Map<String, String[]> expectedRequestParameters = new HashMap<String, String[]>();
    expectedRequestParameters.put("a", new String[]{"a"});
    expectedRequestParameters.put("b", new String[]{"b"});
    assertMapEquals(expectedRequestParameters, renderContext2.getQueryParams());
  }

  @Test
  public void testOptional() throws Exception {
    Router router = new Router();
    router.append("?a={<a>foo}");

    //
    assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/", Collections.<String, String[]>emptyMap()));
    assertEquals(Collections.singletonMap(Names.FOO, "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));

    //
    URIHelper renderContext1 = new URIHelper();
    router.render(Collections.<QualifiedName, String>emptyMap(), renderContext1.writer);
    assertEquals("/", renderContext1.getPath());
    assertEquals(null, renderContext1.getQueryParams());
    URIHelper renderContext2 = new URIHelper();
    router.render(Collections.singletonMap(Names.FOO, "a"), renderContext2.writer);
    assertEquals("/", renderContext2.getPath());
    assertMapEquals(Collections.singletonMap("a", new String[]{"a"}), renderContext2.getQueryParams());
  }

  @Test
  public void testMatchDescendantOfRootParameters() throws Exception {
    Router router = new Router();
    router.append("?a={<a>foo}").append("/a?b={<b>bar}");

    //
    URIHelper renderContext = new URIHelper();
    Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
    parameters.put(Names.FOO, "a");
    parameters.put(Names.BAR, "b");
    router.render(parameters, renderContext.writer);
    assertEquals("/a", renderContext.getPath());
    Map<String, String[]> expectedRequestParameters = new HashMap<String, String[]>();
    expectedRequestParameters.put("a", new String[]{"a"});
    expectedRequestParameters.put("b", new String[]{"b"});
    assertMapEquals(expectedRequestParameters, renderContext.getQueryParams());
  }

  @Test
  public void testLiteralMatch() throws Exception {
    Router router = new Router();
    router.append("?a={<foo_value>[o]foo}");

    //
    Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
    parameters.put(Names.FOO, "foo_value");
    URIHelper rc = new URIHelper();
    router.render(parameters, rc.writer);
    assertEquals("/", rc.getPath());
    assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
    assertEquals(Collections.singletonList("foo_value"), Arrays.asList(rc.getQueryParams().get("a")));
    Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"foo_value"}));
    assertNotNull(a);
    assertEquals(Collections.singleton(Names.FOO), a.keySet());
    assertEquals("foo_value", a.get(Names.FOO));

    //
    parameters = new HashMap<QualifiedName, String>();
    parameters.put(Names.FOO, "bar_value");
    rc.reset();
    router.render(parameters, rc.writer);
    assertEquals("", rc.getPath());
    assertEquals(null, rc.getQueryParams());
    a = router.route("/", Collections.singletonMap("a", new String[]{"bar_value"}));
    assertNull(a);
  }

  @Test
  public void testCanonical() throws Exception {
    Router router = new Router();
    router.append("?a={[o]foo}");

    //
    Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
    parameters.put(Names.FOO, "bar");
    URIHelper rc = new URIHelper();
    router.render(parameters, rc.writer);
    assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
    assertEquals(Collections.singletonList("bar"), Arrays.asList(rc.getQueryParams().get("a")));
    Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"bar"}));
    assertNotNull(a);
    assertEquals(Collections.singleton(Names.FOO), a.keySet());
    assertEquals("bar", a.get(Names.FOO));

    //
    parameters = new HashMap<QualifiedName, String>();
    parameters.put(Names.FOO, "");
    rc.reset();
    router.render(parameters, rc.writer);
    assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
    assertEquals(Collections.singletonList(""), Arrays.asList(rc.getQueryParams().get("a")));
    a = router.route("/", Collections.singletonMap("a", new String[]{""}));
    assertNotNull(a);
    assertEquals(Collections.singleton(Names.FOO), a.keySet());
    assertEquals("", a.get(Names.FOO));

    //
    parameters = new HashMap<QualifiedName, String>();
    rc.reset();
    router.render(parameters, rc.writer);
    assertEquals(null, rc.getQueryParams());
    a = router.route("/");
    assertNotNull(a);
    assertEquals(Collections.<QualifiedName>emptySet(), a.keySet());
  }
}
