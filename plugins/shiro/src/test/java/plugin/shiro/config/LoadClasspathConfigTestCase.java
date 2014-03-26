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
package plugin.shiro.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import plugin.shiro.AbstractShiroTestCase;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class LoadClasspathConfigTestCase extends AbstractShiroTestCase {
  @Drone
  WebDriver driver;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment("plugin.shiro.config.classpath");
  }

  @Test
  @RunAsClient
  public void test() throws Exception {
    driver.get(deploymentURL.toString());
    assertTrue(manager instanceof MySecurityManager);
    assertTrue(manager.getRememberMeManager() instanceof MyRememberMe);
    assertEquals(1, manager.getRealms().size());
    assertTrue(manager.getRealms().iterator().next() instanceof MyRealm);
  }
}
