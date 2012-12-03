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

package juzu.impl.bridge.portlet;

import juzu.impl.common.Tools;
import juzu.test.JavaFile;
import juzu.test.protocol.portlet.AbstractPortletTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LiveModeTestCase extends AbstractPortletTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createPortletDeployment(true, "bridge.portlet.render");
  }

  @ArquillianResource
  URL deploymentURL;

  @Drone
  WebDriver driver;

  @Test
  public void testChange() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/JuzuPortlet").toURL();
    driver.get(url.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertEquals(1, Tools.count(body.getText(), "pass"));

    //
    JavaFile pkgFile = getCompiler().assertSource("bridge", "portlet", "render", "A.java");
    pkgFile.assertSave(pkgFile.assertContent().replace("pass", "pass2"));
    driver.get(url.toString());
    body = driver.findElement(By.tagName("body"));
    assertEquals(1, Tools.count(body.getText(), "pass2"));

    //
    pkgFile.assertSave(pkgFile.assertContent().replace("pass2", "pass3"));
    driver.get(url.toString());
    body = driver.findElement(By.tagName("body"));
    assertEquals(1, Tools.count(body.getText(), "pass3"));
  }
}
