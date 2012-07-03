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
import java.util.Map;

import static juzu.impl.router.metadata.DescriptorBuilder.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalTestCase extends AbstractControllerTestCase {

  /** . */
  public static final String LANG_PATTERN = "([A-Za-z]{2}(-[A-Za-z]{2})?)?";

  @Test
  public void testLanguage1() throws Exception {
    Router router = router().add(
        route("/public/{gtn:lang}").
            with(pathParam("gtn:lang").matchedBy(LANG_PATTERN).preservePath())).
        build();

    //
    assertEquals(Collections.singletonMap(Names.GTN_LANG, ""), router.route("/public"));
    assertEquals(Collections.singletonMap(Names.GTN_LANG, "fr"), router.route("/public/fr"));
    assertEquals(Collections.singletonMap(Names.GTN_LANG, "fr-FR"), router.route("/public/fr-FR"));
  }

  @Test
  public void testLanguage2() throws Exception {
    Router router = router().
        add(route("/{gtn:lang}/public").
            with(pathParam("gtn:lang").matchedBy(LANG_PATTERN))).
        build();

    //
    assertEquals(Collections.singletonMap(Names.GTN_LANG, ""), router.route("/public"));
    assertNull(router.route("/f/public"));
    assertEquals(Collections.singletonMap(Names.GTN_LANG, "fr"), router.route("/fr/public"));
    assertEquals("/public", router.render(Collections.singletonMap(Names.GTN_LANG, "")));
    assertEquals("", router.render(Collections.singletonMap(Names.GTN_LANG, "f")));
    assertEquals("/fr/public", router.render(Collections.singletonMap(Names.GTN_LANG, "fr")));
    assertEquals("/fr-FR/public", router.render(Collections.singletonMap(Names.GTN_LANG, "fr-FR")));
  }

  @Test
  public void testLanguage3() throws Exception {
    Router router = router().
        add(route("/public/{gtn:lang}/{gtn:sitename}{gtn:path}")
            .with(pathParam("gtn:lang").matchedBy(LANG_PATTERN).preservePath())
            .with(pathParam("gtn:path").matchedBy(".*").preservePath())).
        build();

    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_LANG, "fr");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
//      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/public"));
    assertEquals(expectedParameters, router.route("/public/fr/classic/home"));

    expectedParameters.put(Names.GTN_PATH, "");
    assertEquals(expectedParameters, router.route("/public/fr/classic"));

    expectedParameters.put(Names.GTN_LANG, "");
    expectedParameters.put(Names.GTN_PATH, "/home");
    assertEquals(expectedParameters, router.route("/public/classic/home"));
  }

  @Test
  public void testDuplicateRouteWithDifferentRouteParam() throws Exception {
    Router router = router().add(
        route("/").with(routeParam("foo").withValue("foo_1")).with(requestParam("bar").named("bar").matchedByLiteral("bar_value")),
        route("/").with(routeParam("foo").withValue("foo_2"))
    ).build();

    //
    Map<QualifiedName, String> expected = new HashMap<QualifiedName, String>();
    expected.put(Names.FOO, "foo_1");
    expected.put(Names.BAR, "bar_value");
    assertEquals(expected, router.route("/", Collections.singletonMap("bar", new String[]{"bar_value"})));
    URIHelper rc = new URIHelper();
    router.render(expected, rc.writer);
    assertEquals("/", rc.getPath());
    assertMapEquals(Collections.<String, String[]>singletonMap("bar", new String[]{"bar_value"}), rc.getQueryParams());

    //
    expected = new HashMap<QualifiedName, String>();
    expected.put(Names.FOO, "foo_2");
    assertEquals(expected, router.route("/", Collections.singletonMap("bar", new String[]{"flabbergast"})));
    rc = new URIHelper();
    router.render(expected, rc.writer);
    assertEquals("/", rc.getPath());
    assertEquals(null, rc.getQueryParams());
  }

  @Test
  public void testJSMin() throws Exception {
    Router router = router().add(
        route("/foo{gtn:min}.js").with(pathParam("gtn:min").matchedBy("-(min)|").captureGroup(true))
    ).build();

    //
    assertEquals(Collections.singletonMap(Names.GTN_MIN, "min"), router.route("/foo-min.js"));
    assertEquals(Collections.singletonMap(Names.GTN_MIN, ""), router.route("/foo.js"));
    assertNull(router.route("/foo-max.js"));

    //
    assertEquals("/foo-min.js", router.render(Collections.singletonMap(Names.GTN_MIN, "min")));
    assertEquals("/foo.js", router.render(Collections.singletonMap(Names.GTN_MIN, "")));
    assertEquals("", router.render(Collections.singletonMap(Names.GTN_MIN, "max")));
  }
}
