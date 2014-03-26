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
package plugin.shiro;

import juzu.test.AbstractWebTestCase;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.junit.AfterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public abstract class AbstractShiroTestCase extends AbstractWebTestCase {
  public static DefaultSecurityManager manager;

  @AfterClass
  public static void destroy() {
    manager.destroy();
  }

  protected void waitForPresent(WebDriver driver, String text) throws InterruptedException {
      for (int second = 0;; second++) {
        if (second >= 60)
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
