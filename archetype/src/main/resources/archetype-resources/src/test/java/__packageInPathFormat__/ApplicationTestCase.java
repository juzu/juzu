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