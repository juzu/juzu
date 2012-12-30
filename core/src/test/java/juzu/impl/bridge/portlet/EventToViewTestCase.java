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

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EventToViewTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL portletXML = Thread.currentThread().getContextClassLoader().getResource("bridge/portlet/event/view/portlet.xml");
    return createPortletDeployment("bridge.portlet.event.view", portletXML);
  }

  @ArquillianResource
  URL deploymentURL;

  @Drone
  WebDriver driver;

  @Test
  public void testFoo() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/JuzuPortlet").toURL();
    driver.get(url.toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    String src = driver.getPageSource();
    assertTrue("Was expecting " + src + " to contain 'done:foo'", src.contains("done: foo"));
  }
}
