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
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractAssetTestCase extends AbstractWebTestCase {

  @Drone
  WebDriver driver;

  protected abstract String getExpectedAsset();

  @Test
  public void testSatisfied() throws Exception {
    URL url = applicationURL();
    driver.get(url.toString());
    List<WebElement> scripts = driver.findElements(By.tagName("script"));
    String expected = getExpectedAsset();
    if (expected != null) {
      if (scripts.size() != 1) {
        throw failure("Was expecting scripts to match the single asset " + expected + " instead of being " + scripts);
      } else {
        String src  = scripts.get(0).getAttribute("src");
        assertTrue("Was expecting " + src + " to end with " + expected, src.endsWith(expected));
        url = new URL(url, src);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.connect();
        assertEquals(200, conn.getResponseCode());
      }
    } else {
      assertEquals(Collections.emptyList(), scripts);
    }
  }
}
