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

package ${package};

import java.net.URL;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import juzu.arquillian.Helper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static junit.framework.Assert.*;

@RunWith(Arquillian.class)
public class ApplicationTestCase {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive war = Helper.createBaseServletDeployment("${juzuInject}");
    war.addPackages(true, Application.class.getPackage());
    return war;
  }

  @Drone
  WebDriver driver;

  @ArquillianResource
  URL deploymentURL;

  @Test
  @RunAsClient
  public void testFoo() {
    driver.get(deploymentURL.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertEquals("Hello World", body.getText());
  }
}