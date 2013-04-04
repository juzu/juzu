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
public class PortalConfigurationTestCase extends AbstractControllerTestCase {

  /** . */
  private RouterAssert router;

  /** . */
  private Route portal;

  /** . */
  private Route group;

  /** . */
  private Route user;

  @Override
  public void setUp() throws Exception {
    this.router = new RouterAssert();

    Map<String, PathParam.Builder> params = Collections.singletonMap(Names.GTN_PATH, PathParam.matching(".*").preservePath(true));

    portal = router.append("/private/{gtn:sitetype}/{gtn:sitename}{gtn:path}", params);
    group = router.append("/groups/{gtn:sitetype}/{gtn:sitename}{gtn:path}", params);
    user = router.append("/users/{gtn:sitetype}/{gtn:sitename}{gtn:path}", params);
  }

  @Test
  public void testComponent() throws Exception {
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_SITETYPE, "portal");
    expectedParameters.put(Names.GTN_PATH, "/");

    //
    router.assertRoute(portal, expectedParameters, "/private/portal/classic/");
    assertEquals("/private/portal/classic/", portal.matches(expectedParameters).render());
  }

  @Test
  public void testPrivateClassic() throws Exception {
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_SITETYPE, "portal");
    expectedParameters.put(Names.GTN_PATH, "");

    //
    router.assertRoute(portal, expectedParameters, "/private/portal/classic");
    assertEquals("/private/portal/classic", portal.matches(expectedParameters).render());
  }

  @Test
  public void testPrivateClassicSlash() throws Exception {
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_SITETYPE, "portal");
    expectedParameters.put(Names.GTN_PATH, "/");

    //
    router.assertRoute(portal, expectedParameters, "/private/portal/classic/");
    assertEquals("/private/portal/classic/", portal.matches(expectedParameters).render());
  }

  @Test
  public void testPrivateClassicHome() throws Exception {
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_SITETYPE, "portal");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
    router.assertRoute(portal, expectedParameters, "/private/portal/classic/home");
    assertEquals("/private/portal/classic/home", portal.matches(expectedParameters).render());
  }

  @Test
  public void testSiteType() throws Exception {
    Map<String, String> expectedParameters = new HashMap<String, String>();
    expectedParameters.put(Names.GTN_SITETYPE, "group");
    expectedParameters.put(Names.GTN_SITENAME, "platform");
    expectedParameters.put(Names.GTN_PATH, "/administration/registry");

    //
    router.assertRoute(portal, expectedParameters, "/private/group/platform/administration/registry");
    assertEquals("/private/group/platform/administration/registry", portal.matches(expectedParameters).render());

    Map<String, String> expectedParameters1 = new HashMap<String, String>();
    expectedParameters1.put(Names.GTN_SITETYPE, "user");
    expectedParameters1.put(Names.GTN_SITENAME, "root");
    expectedParameters1.put(Names.GTN_PATH, "/tab_0");

    //
    router.assertRoute(portal, expectedParameters1, "/private/user/root/tab_0");
    assertEquals("/private/user/root/tab_0", portal.matches(expectedParameters1).render());
  }
}
