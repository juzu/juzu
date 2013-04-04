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

package juzu.plugin.servlet;

import juzu.plugin.servlet.impl.ServletMetaModelPlugin;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletPluginTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws Exception {
    File f = new File(ServletMetaModelPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    WebArchive war = createServletDeployment(true, "plugin.servlet.base");
    war.delete("WEB-INF/web.xml");
    war.addAsLibrary(f);
    return war;
  }

  @Drone
  WebDriver driver;

  @Test
  public void testBase() throws Exception {
    driver.get(applicationURL().toString());
    assertEquals("pass", driver.findElement(By.tagName("body")).getText());
  }
}
