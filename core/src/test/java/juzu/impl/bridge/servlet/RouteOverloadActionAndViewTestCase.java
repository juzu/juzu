/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.servlet;

import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteOverloadActionAndViewTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "bridge.servlet.route.overload.actionandview");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testRender() throws Exception {
    driver.get(applicationURL().toString());
    WebElement trigger = driver.findElement(By.tagName("body"));
    URL url = new URL(trigger.getText());
    assertEquals(applicationURL("/foo").getPath(), url.getPath());
    assertNull(url.getQuery());
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setInstanceFollowRedirects(false);
    conn.setRequestMethod("POST");
    conn.connect();
    assertEquals(302, conn.getResponseCode());
    url = new URL(Tools.responseHeaders(conn).get("Location"));
    assertEquals(applicationURL("/foo").getPath(), url.getPath());
    assertNull(url.getQuery());
    driver.get(url.toString());
    String pass = driver.findElement(By.tagName("body")).getText();
    assertEquals("pass", pass);
  }
}
