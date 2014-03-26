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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
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
public class AuthcWithRequireAtCtrlTestCase extends AbstractShiroTestCase {
  /** . */
  public static Subject currentUser;

  /** . */
  public static Exception exception;

  @Drone
  WebDriver driver;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(InjectorProvider.GUICE, "plugin.shiro.require.controller2");
    war.addPackage(SimpleRealm.class.getPackage());
    return war;
  }

  @Test
  @RunAsClient
  public void test() throws Exception {
    driver.get(deploymentURL.toString());
    assertNull(exception);

    WebElement failed = driver.findElement(By.id("failed"));
    failed.click();
    assertTrue(exception instanceof AuthenticationException);
    assertNull(currentUser.getPrincipal());

    driver.get(deploymentURL.toString());
    WebElement login = driver.findElement(By.id("login"));
    login.click();
    assertEquals("root", currentUser.getPrincipal());
    assertNull(exception);

    driver.get(deploymentURL.toString());
    assertTrue(exception instanceof AuthorizationException);

    WebElement logout = driver.findElement(By.id("logout"));
    logout.click();
    assertNull(currentUser.getPrincipal());
    assertTrue(exception instanceof AuthorizationException);

  }
}
