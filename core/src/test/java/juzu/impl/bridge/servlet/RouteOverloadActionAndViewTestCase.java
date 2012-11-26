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

import juzu.impl.common.Tools;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteOverloadActionAndViewTestCase extends AbstractStandaloneTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment("bridge.servlet.route.overload.actionandview");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testRender() throws Exception {
    driver.get(applicationURL().toString());
    WebElement trigger = driver.findElement(By.tagName("body"));
    URL url = new URL(trigger.getText());
    assertEquals(applicationURL("/foo").getPath(), url.getPath());
    assertNull(url.getQuery());
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setInstanceFollowRedirects(false);
    conn.setRequestMethod("POST");
    conn.connect();
    assertEquals(302, conn.getResponseCode());
    url = new URL(Tools.responseHeaders(conn).get("Location"));
    System.out.println("url = " + url);
    System.out.println("url = " + url);
    System.out.println("url = " + url);
    System.out.println("url = " + url);
    assertEquals(applicationURL("/foo").getPath(), url.getPath());
    assertNull(url.getQuery());
    driver.get(url.toString());
    String pass = driver.findElement(By.tagName("body")).getText();
    assertEquals("pass", pass);
  }
}
