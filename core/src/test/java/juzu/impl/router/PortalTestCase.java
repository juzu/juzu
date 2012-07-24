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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalTestCase extends AbstractControllerTestCase {

  /** . */
  public static final String LANG_PATTERN = "([A-Za-z]{2}(-[A-Za-z]{2})?)?";

  @Test
  public void testLanguage1() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/public/{<" + LANG_PATTERN + ">[p]gtn:lang}");

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, ""), "/public");
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr"), "/public/fr");
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr-FR"), "/public/fr-FR");
  }

  @Test
  public void testLanguage2() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/{<" + LANG_PATTERN + ">gtn:lang}/public");

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, ""), "/public");
    assertNull(router.route("/f/public"));
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr"), "/fr/public");
    assertEquals("/public", router.render(Collections.singletonMap(Names.GTN_LANG, "")));
    assertEquals("", router.render(Collections.singletonMap(Names.GTN_LANG, "f")));
    assertEquals("/fr/public", router.render(Collections.singletonMap(Names.GTN_LANG, "fr")));
    assertEquals("/fr-FR/public", router.render(Collections.singletonMap(Names.GTN_LANG, "fr-FR")));
  }

  @Test
  public void testLanguage3() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/public/{<" + LANG_PATTERN + ">[p]gtn:lang}").append("{gtn:sitename}{<.*>[p]gtn:path}");

    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_LANG, "fr");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
//      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/public"));
    router.assertRoute(expectedParameters, "/public/fr/classic/home");

    expectedParameters.put(Names.GTN_PATH, "");
    router.assertRoute(expectedParameters, "/public/fr/classic");

    expectedParameters.put(Names.GTN_LANG, "");
    expectedParameters.put(Names.GTN_PATH, "/home");
    router.assertRoute(expectedParameters, "/public/classic/home");
  }

  @Test
  public void testDuplicateRouteWithDifferentRouteParam() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("?bar={<bar_value>bar}").addParam("foo", "foo_1");
    router.append("/").addParam("foo", "foo_2");

    //
    Map<QualifiedName, String> expected = new HashMap<QualifiedName, String>();
    expected.put(Names.FOO, "foo_1");
    expected.put(Names.BAR, "bar_value");
    router.assertRoute(expected, "/", Collections.singletonMap("bar", "bar_value"));
    URIHelper rc = new URIHelper();
    router.render(expected, rc.writer);
    assertEquals("/", rc.getPath());
    assertMapEquals(Collections.<String, String[]>singletonMap("bar", new String[]{"bar_value"}), rc.getQueryParams());

    //
    expected = new HashMap<QualifiedName, String>();
    expected.put(Names.FOO, "foo_2");
    router.assertRoute(expected, "/", Collections.singletonMap("bar", "flabbergast"));
    rc = new URIHelper();
    router.render(expected, rc.writer);
    assertEquals("/", rc.getPath());
    assertEquals(null, rc.getQueryParams());
  }

  @Test
  public void testJSMin() throws Exception {
    RouterAssert router = new RouterAssert();
    router.append("/foo{<-(min)|>[c]gtn:min}.js");

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_MIN, "min"), "/foo-min.js");
    router.assertRoute(Collections.singletonMap(Names.GTN_MIN, ""), "/foo.js");
    assertNull(router.route("/foo-max.js"));

    //
    assertEquals("/foo-min.js", router.render(Collections.singletonMap(Names.GTN_MIN, "min")));
    assertEquals("/foo.js", router.render(Collections.singletonMap(Names.GTN_MIN, "")));
    assertEquals("", router.render(Collections.singletonMap(Names.GTN_MIN, "max")));
  }
}
