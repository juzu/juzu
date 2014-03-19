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
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetControllerInjectTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.asset.controller.template");
  }

  @Drone
  WebDriver driver;

  @Test
  public void resolveAsset() throws Exception {
    URL url = applicationURL();
    driver.get(url.toString());
    List<WebElement> scripts = driver.findElements(By.tagName("script"));
    assertEquals(2, scripts.size());
    assertEndsWith("/juzu/assets/plugin/asset/controller/template/assets/test.js", scripts.get(0).getAttribute("src"));
    assertEndsWith("/juzu/assets/plugin/asset/controller/template/assets/foo.js", scripts.get(1).getAttribute("src"));
  }
}
