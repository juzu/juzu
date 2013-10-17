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

package juzu.impl.bridge.runmode;

import juzu.impl.common.RunMode;
import juzu.test.AbstractWebTestCase;
import juzu.test.JavaFile;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractRunModeLiveControllerTestCase extends AbstractWebTestCase {

  public static Boolean SAME_CL_1;
  public static Boolean SAME_CL_2;
  public static RunMode RUN_MODE;

  @Drone
  WebDriver driver;

  protected abstract URL getURL();

  protected abstract int getErrorStatus();

  @Test
  public void testRender() throws Exception {

    //
    driver.get(getURL().toString());
    assertEquals(RunMode.LIVE, RUN_MODE);
    WebElement elt = driver.findElement(By.id("trigger"));
    URL url = new URL(elt.getAttribute("href"));
    driver.get(url.toString());
    assertEquals("ok", driver.findElement(By.tagName("body")).getText());
    assertFalse(SAME_CL_1);
    assertFalse(SAME_CL_2);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    assertEquals(getErrorStatus(), conn.getResponseCode());
    driver.get(url.toString());
    assertEquals("java.lang.RuntimeException: throwed", driver.findElement(By.cssSelector("div.juzu > section > p")).getText().trim());

    // Make a change
    JavaFile pkgFile = getCompiler().assertJavaSource("bridge.runmode.live.controller.A");
    pkgFile.assertSave(pkgFile.assertContent().replace("\"ok\"", "\"OK\""));
    driver.get(applicationURL().toString());
    elt = driver.findElement(By.id("trigger"));
    elt.click();
    assertEquals("OK", driver.findElement(By.tagName("body")).getText());
    assertTrue(SAME_CL_1);
    assertFalse(SAME_CL_2);

    // Now make fail with compilation error
    pkgFile.assertSave(pkgFile.assertContent().replace("public", "_public_"));

    //
    conn = (HttpURLConnection)applicationURL().openConnection();
    assertEquals(getErrorStatus(), conn.getResponseCode());
    driver.get(applicationURL().toString());
    assertNotNull(driver.findElement(By.cssSelector("div.juzu")));
    assertNotNull(elt);

    //
    pkgFile.assertSave(pkgFile.assertContent().replace("_public_", "public"));

    //
    driver.get(applicationURL().toString());
    elt = driver.findElement(By.id("trigger"));
    elt.click();
    assertEquals("OK", driver.findElement(By.tagName("body")).getText());
  }
}
