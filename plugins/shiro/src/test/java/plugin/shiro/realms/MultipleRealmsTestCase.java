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
package plugin.shiro.realms;

import juzu.impl.inject.spi.InjectorProvider;

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
public class MultipleRealmsTestCase extends AbstractShiroTestCase {
  @Drone
  WebDriver driver;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(InjectorProvider.GUICE, "plugin.shiro.realms");
    war.addPackage(SimpleRealm.class.getPackage());
    return war;
  }

  @Test
  @RunAsClient
  public void test() throws InterruptedException {
    driver.get(deploymentURL.toString());
    assertEquals(2, manager.getRealms().size());
    WebElement john = driver.findElement(By.id("john"));
    john.click();
    waitForPresent("can not access");

    driver.get(deploymentURL.toString());
    WebElement marry = driver.findElement(By.id("marry"));
    marry.click();
    waitForPresent("ok");
  }

  private void waitForPresent(String text) throws InterruptedException {
    for (int second = 0;; second++) {
      if (second >= 10)
        fail("timeout");
      try {
        if (driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*" + text + "[\\s\\S]*$")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
  }
}
