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
package plugin.shiro.authz;

import juzu.impl.inject.spi.InjectorProvider;

import org.apache.shiro.authz.AuthorizationException;
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
public class RequireAtControllerTestCase extends AbstractShiroTestCase {

  public static Exception exception;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(InjectorProvider.SPRING, "plugin.shiro.require.controller1");
    war.addPackage(SimpleRealm.class.getPackage());
    return war;
  }

  @Drone
  WebDriver driver;

  @Test
  @RunAsClient
  public void test() throws Exception {
    driver.get(deploymentURL.toString());
    assertNotNull(exception);
    assertTrue(exception instanceof AuthorizationException);

    WebElement view = driver.findElement(By.id("view"));
    view.click();
    waitForPresent("Unauthorized");

    driver.get(deploymentURL.toString());
    WebElement resource = driver.findElement(By.id("resource"));
    resource.click();
    waitForPresent("Unauthorized");

    driver.get(deploymentURL.toString());
    WebElement action = driver.findElement(By.id("action"));
    action.click();
    waitForPresent("Unauthorized");
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
