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
