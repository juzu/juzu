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

package juzu.impl.plugin.asset;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL jquery = AbstractWebTestCase.class.getResource("jquery-1.7.1.js");
    URL test = AbstractWebTestCase.class.getResource("test.js");
    URL css = AbstractWebTestCase.class.getResource("main.css");
    URL less = AbstractWebTestCase.class.getResource("main.less");
    return createServletDeployment(true, "plugin.asset.ajax").
        addAsWebResource(jquery, "jquery.js").
        addAsWebResource(test, "test.js").
        addAsWebResource(css, "main.css").
        addAsWebResource(css, "main.less");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testAjax() throws Exception {
    driver.get(applicationURL().toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    String bar = driver.findElement(By.id("foo")).getText();
    assertEquals("bar", bar);
  }
}
