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

package juzu.impl.bridge.servlet;

import juzu.test.protocol.servlet.AbstractServletTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.net.HttpURLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteModuleNoMountTestCase extends AbstractServletTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(false, "bridge.servlet.route.module.nomount");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testRenderRoot() throws Exception {
    HttpURLConnection conn = (HttpURLConnection)deploymentURL.openConnection();
    conn.connect();
    assertEquals(404, conn.getResponseCode());
  }

  @Test
  public void testRenderPath() throws Exception {
    HttpURLConnection conn = (HttpURLConnection)deploymentURL.toURI().resolve("bar").toURL().openConnection();
    conn.connect();
    assertEquals(404, conn.getResponseCode());
  }

  @Test
  public void testRenderAny() throws Exception {
    HttpURLConnection conn = (HttpURLConnection)deploymentURL.toURI().resolve("any").toURL().openConnection();
    conn.connect();
    assertEquals(404, conn.getResponseCode());
  }
}
