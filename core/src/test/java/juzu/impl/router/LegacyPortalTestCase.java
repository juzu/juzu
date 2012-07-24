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
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LegacyPortalTestCase extends AbstractTestCase {

  /** . */
  private RouterAssert router;

  @Override
  public void setUp() throws Exception {
    router = new RouterAssert();
    Route portal = router.append("/").addParam("gtn:handler", "portal");
    portal.append("/public/{gtn:sitename}{<.*>[p]gtn:path}").addParam("gtn:access", "public");
    portal.append("/private/{gtn:sitename}{<.*>[p]gtn:path}").addParam("gtn:access", "private");
  }

  @Test
  public void testPrivateClassicComponent() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "");

    //
    router.assertRoute(expectedParameters, "/private/classic");
    assertEquals("/private/classic", router.render(expectedParameters));
  }

  @Test
  public void testPrivateClassic() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "");

    //
    router.assertRoute(expectedParameters, "/private/classic");
    assertEquals("/private/classic", router.render(expectedParameters));
  }

  @Test
  public void testPrivateClassicSlash() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "/");

    //
    router.assertRoute(expectedParameters, "/private/classic/");
    assertEquals("/private/classic/", router.render(expectedParameters));
  }

  @Test
  public void testPrivateClassicSlashComponent() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "/");

    //
    router.assertRoute(expectedParameters, "/private/classic/");
    assertEquals("/private/classic/", router.render(expectedParameters));
  }

  @Test
  public void testPrivateClassicHome() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
    router.assertRoute(expectedParameters, "/private/classic/home");
    assertEquals("/private/classic/home", router.render(expectedParameters));
  }

  @Test
  public void testPrivateClassicHomeComponent() throws Exception {
    Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
    expectedParameters.put(Names.GTN_HANDLER, "portal");
    expectedParameters.put(Names.GTN_SITENAME, "classic");
    expectedParameters.put(Names.GTN_ACCESS, "private");
    expectedParameters.put(Names.GTN_PATH, "/home");

    //
    router.assertRoute(expectedParameters, "/private/classic/home");
    assertEquals("/private/classic/home", router.render(expectedParameters));
  }
}
