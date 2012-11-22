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

import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteModuleMountAsDefaultMountTestCase extends AbstractStandaloneTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "bridge.servlet.route.module.mountmount.app1", "bridge.servlet.route.module.mountmount.app2");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testRenderDefault() throws Exception {
    driver.get(deploymentURL.toString());
    String index = driver.findElement(By.tagName("body")).getText();
    assertEquals("app1:index", index);
  }

  @Test
  public void testRenderIndexApp1() throws Exception {
    URL url = deploymentURL.toURI().resolve("app1").toURL();
    driver.get(url.toString());
    String index = driver.findElement(By.tagName("body")).getText();
    assertEquals("app1:index", index);
  }

  @Test
  public void testRenderRouteApp1() throws Exception {
    URL url = deploymentURL.toURI().resolve("app1/bar").toURL();
    driver.get(url.toString());
    String index = driver.findElement(By.tagName("body")).getText();
    assertEquals("app1:bar", index);
  }

  @Test
  public void testRenderIndexApp2() throws Exception {
    URL url = deploymentURL.toURI().resolve("app2").toURL();
    driver.get(url.toString());
    String index = driver.findElement(By.tagName("body")).getText();
    assertEquals("app2:index", index);
  }

  @Test
  public void testRenderRouteApp2() throws Exception {
    URL url = deploymentURL.toURI().resolve("app2/bar").toURL();
    driver.get(url.toString());
    String index = driver.findElement(By.tagName("body")).getText();
    assertEquals("app2:bar", index);
  }
}
