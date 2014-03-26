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

import java.util.HashMap;
import java.util.Map;

import juzu.impl.inject.spi.InjectorProvider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.SimpleRealm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AuthorizationTestCase extends AbstractShiroTestCase {
  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(InjectorProvider.SPRING, "plugin.shiro.authz");
    war.addPackages(true, SimpleRealm.class.getPackage());
    return war;
  }

  private static Map<String, String> urls = new HashMap<String, String>();

  @Drone
  private WebDriver driver;

  public static String missingRole;

  public static String missingPermission;

  @Before
  public void init() {
    driver.get(deploymentURL.toString());
    urls.put("root", driver.findElement(By.id("root")).getAttribute("href"));
    urls.put("john", driver.findElement(By.id("john")).getAttribute("href"));
    urls.put("logout", driver.findElement(By.id("logout")).getAttribute("href"));
    urls.put("role1", driver.findElement(By.id("role1")).getAttribute("href"));
    urls.put("role2", driver.findElement(By.id("role2")).getAttribute("href"));
    urls.put("role1or2", driver.findElement(By.id("role1or2")).getAttribute("href"));
    urls.put("role1and2", driver.findElement(By.id("role1and2")).getAttribute("href"));
    urls.put("permission1", driver.findElement(By.id("permission1")).getAttribute("href"));
    urls.put("permission2", driver.findElement(By.id("permission2")).getAttribute("href"));
    urls.put("role2andPerm1", driver.findElement(By.id("role2andPerm1")).getAttribute("href"));

    missingRole = null;
    missingPermission = null;
  }

  @Test
  @RunAsClient
  public void testRoot() throws Exception {
    // login root
    driver.get(urls.get("root"));

    String[] available =
      new String[]{urls.get("role1"), urls.get("role2"), urls.get("role1or2"), urls.get("role1and2"),
        urls.get("permission1"), urls.get("permission2"), urls.get("role2andPerm1")};
    for (String url : available) {
      driver.get(url);
      assertEquals("ok", driver.findElement(By.tagName("body")).getText());
    }
  }

  @Test
  @RunAsClient
  public void testJohn() throws Exception {
    // login john
    driver.get(urls.get("john"));

    driver.get(urls.get("role1"));
    assertEquals("Cannot access", driver.findElement(By.tagName("body")).getText());
    assertEquals("role1", AuthorizationTestCase.missingRole);
    AuthorizationTestCase.missingRole = null;

    driver.get(urls.get("role2"));
    assertEquals("ok", driver.findElement(By.tagName("body")).getText());

    driver.get(urls.get("permission1"));
    assertEquals("Cannot access", driver.findElement(By.tagName("body")).getText());
    assertEquals("permission1", AuthorizationTestCase.missingPermission);
    AuthorizationTestCase.missingPermission = null;

    driver.get(urls.get("permission2"));
    assertEquals("ok", driver.findElement(By.tagName("body")).getText());

    driver.get(urls.get("role1or2"));
    assertEquals("ok", driver.findElement(By.tagName("body")).getText());

    driver.get(urls.get("role1and2"));
    assertEquals("Cannot access", driver.findElement(By.tagName("body")).getText());
    assertEquals("role1 AND role2", AuthorizationTestCase.missingRole);
    AuthorizationTestCase.missingRole = null;

    driver.get(urls.get("role2andPerm1"));
    assertEquals("Cannot access", driver.findElement(By.tagName("body")).getText());
    assertEquals("role2", AuthorizationTestCase.missingRole);
    assertEquals("permission1", AuthorizationTestCase.missingPermission);
  }
}
