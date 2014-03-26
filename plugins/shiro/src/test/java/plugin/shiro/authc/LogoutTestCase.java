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
package plugin.shiro.authc;

import juzu.impl.inject.spi.InjectorProvider;

import org.apache.shiro.subject.Subject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.SimpleRealm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class LogoutTestCase extends AbstractShiroTestCase {
  @Drone
  WebDriver driver;

  public static Subject currentUser;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(InjectorProvider.WELD, "plugin.shiro.authc.logout");
    war.addPackages(true, SimpleRealm.class.getPackage());
    return war;
  }

  @Test
  @RunAsClient
  public void testLogout() throws Exception {
    driver.get(deploymentURL.toString());
    assertEquals("root", currentUser.getPrincipal().toString());
    WebElement trigger = driver.findElement(By.id("logout"));
    trigger.click();
    assertNull(currentUser.getPrincipal());
  }
}
