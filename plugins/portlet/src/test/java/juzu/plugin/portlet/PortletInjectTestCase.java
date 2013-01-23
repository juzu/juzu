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

package juzu.plugin.portlet;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletInjectTestCase extends AbstractWebTestCase {

  /** . */
  public static ResourceBundle bundle;

  /** . */
  public static boolean prefs;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createPortletDeployment("plugin.portlet.inject");
  }

  @Test
  public void testInjection() throws Exception {
    bundle = null;
    prefs = false;
    HttpURLConnection conn = (HttpURLConnection)getPortletURL().openConnection();
    assertEquals(200, conn.getResponseCode());
    assertNotNull(bundle);
    assertTrue(prefs);
  }
}
