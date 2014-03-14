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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AmdMaxAgeTestCase extends AbstractWebTestCase {
  
  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.amd.maxage");
  }

  @Drone
  WebDriver driver;

  @Test
  @RunAsClient
  public void test() throws Exception {
    URL applicationURL = applicationURL();
    driver.get(applicationURL.toString());
    WebElement elt = driver.findElement(By.id("Foo"));
    URL url = new URL(applicationURL.getProtocol(), applicationURL.getHost(), applicationURL.getPort(), elt.getText());
    HttpGet get = new HttpGet(url.toURI());
    HttpResponse response = HttpClientBuilder.create().build().execute(get);
    Header[] cacheControl = response.getHeaders("Cache-Control");
    assertEquals(1, cacheControl.length);
    assertEquals("max-age=1000", cacheControl[0].getValue());
  }
}
