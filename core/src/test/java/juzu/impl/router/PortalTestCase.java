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
