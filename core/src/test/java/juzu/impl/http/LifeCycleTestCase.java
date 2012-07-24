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

package juzu.impl.http;

import juzu.test.protocol.http.AbstractHttpTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleTestCase extends AbstractHttpTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testLifeCycle() throws Exception {
    assertDeploy("http", "lifecycle");
    driver.get(deploymentURL.toString());
    String actionURL = driver.findElement(By.tagName("body")).getText();
    assertTrue(actionURL.length() > 0);
    driver.get(actionURL);
    String resourceURL = driver.findElement(By.tagName("body")).getText();
    assertTrue(resourceURL.length() > 0);
    driver.get(resourceURL);
    String done = driver.findElement(By.tagName("body")).getText();
    assertEquals("done", done);
  }
}
