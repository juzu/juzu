/*
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
package juzu.impl.plugin.amd;

import juzu.test.AbstractWebTestCase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDDefineTestCase extends AbstractWebTestCase {
  
  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(true, "plugin.amd.define");
    return war;
  }
  
  @Drone
  FirefoxDriver driver;

  @Test @RunAsClient
  public void test() throws Exception {
    driver.get(applicationURL().toString());
    assertEquals("Hello World", driver.switchTo().alert().getText());
    driver.switchTo().alert().accept();
    
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    assertEquals("Hello", driver.switchTo().alert().getText());
    
    //
//    UserAgent ua = assertInitialPage();
//    HtmlPage page = ua.getHomePage();
//    List<String> alerts = ua.getAlerts(page);
//    assertEquals(Arrays.asList("Hello World"), alerts);
    
//    HtmlAnchor trigger2 = (HtmlAnchor)page.getElementById("trigger");
//    trigger2.click();
//    alerts = ua.getAlerts(page);
//    assertEquals(Arrays.asList("Hello"), alerts);
//
//    DomNodeList<HtmlElement> links = page.getElementsByTagName("script");
//    assertEquals(3, links.size());
  }
}
