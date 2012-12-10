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
    router.append("/public/{gtn:lang}", Collections.singletonMap(Names.GTN_LANG, PathParam.matching(LANG_PATTERN).preservePath(true)));

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, ""), "/public");
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr"), "/public/fr");
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr-FR"), "/public/fr-FR");
  }

  @Test
  public void testLanguage2() throws Exception {
    RouterAssert router = new RouterAssert();
    Route r = router.append("/{gtn:lang}/public", Collections.singletonMap(Names.GTN_LANG, PathParam.matching(LANG_PATTERN)));

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, ""), "/public");
    assertNull(router.route("/f/public"));
    router.assertRoute(Collections.singletonMap(Names.GTN_LANG, "fr"), "/fr/public");
    assertEquals("/public", r.matches(Collections.singletonMap(Names.GTN_LANG, "")).render());
    assertNull(r.matches(Collections.singletonMap(Names.GTN_LANG, "f")));
    assertEquals("/fr/public", r.matches(Collections.singletonMap(Names.GTN_LANG, "fr")).render());
    assertEquals("/fr-FR/public", r.matches(Collections.singletonMap(Names.GTN_LANG, "fr-FR")).render());
  }

  @Test
  public void testLanguage3() throws Exception {
    RouterAssert router = new RouterAssert();
    Route r = router.
        append("/public/{gtn:lang}", Collections.singletonMap(Names.GTN_LANG, PathParam.matching(LANG_PATTERN).preservePath(true))).
        append("{gtn:sitename}{gtn:path}", Collections.singletonMap(Names.GTN_PATH, PathParam.matching(".*").preservePath(true)));

    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_LANG, "fr");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
//      assertEquals(Collections.<String, String>emptyMap(), router.route("/public"));
    router.assertRoute(expectedParameters, "/public/fr/classic/home");

    expectedParameters.put(Names.GTN_PATH, "");
    router.assertRoute(expectedParameters, "/public/fr/classic");

    expectedParameters.put(Names.GTN_LANG, "");
    expectedParameters.put(Names.GTN_PATH, "/home");
    router.assertRoute(expectedParameters, "/public/classic/home");
  }

  @Test
  public void testJSMin() throws Exception {
    RouterAssert router = new RouterAssert();
    Route r = router.append("/foo{gtn:min}.js", Collections.singletonMap(Names.GTN_MIN, PathParam.matching("-(min)|").captureGroup(true)));

    //
    router.assertRoute(Collections.singletonMap(Names.GTN_MIN, "min"), "/foo-min.js");
    router.assertRoute(Collections.singletonMap(Names.GTN_MIN, ""), "/foo.js");
    assertNull(router.route("/foo-max.js"));

    //
    assertEquals("/foo-min.js", r.matches(Collections.singletonMap(Names.GTN_MIN, "min")).render());
    assertEquals("/foo.js", r.matches(Collections.singletonMap(Names.GTN_MIN, "")).render());
    assertNull(r.matches(Collections.singletonMap(Names.GTN_MIN, "max")));
  }
}
