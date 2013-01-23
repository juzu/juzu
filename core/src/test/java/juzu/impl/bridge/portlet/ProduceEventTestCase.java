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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProduceEventTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL portletXML = Thread.currentThread().getContextClassLoader().getResource("bridge/portlet/event/produce/portlet.xml");
    return createPortletDeployment("bridge.portlet.event.produce", portletXML);
  }

  @Drone
  WebDriver driver;

  /** . */
  public static final LinkedList<String> eventNames = new LinkedList<String>();

  /** . */
  public static final LinkedList<QName> eventQNames = new LinkedList<QName>();

  /** . */
  public static final LinkedList<Serializable> eventPayloads = new LinkedList<Serializable>();

  @Test
  public void testFoo() throws Exception {
    assertEquals(Collections.emptyList(), eventNames);
    assertEquals(Collections.emptyList(), eventQNames);
    assertEquals(Collections.emptyList(), eventPayloads);
    driver.get(getPortletURL().toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    assertEquals(Arrays.asList("the_event"), eventNames);
    assertEquals(Arrays.asList(new QName("the_event")), eventQNames);
    assertEquals(Arrays.asList(3), eventPayloads);
  }
}
