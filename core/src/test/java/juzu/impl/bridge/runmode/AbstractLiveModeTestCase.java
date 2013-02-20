/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.bridge.runmode;

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
public abstract class AbstractLiveModeTestCase extends AbstractWebTestCase {

  @Drone
  WebDriver driver;

  protected abstract URL getURL();

  protected abstract int getErrorStatus();

  @Test
  public void testRender() throws Exception {
    driver.get(getURL().toString());
    WebElement elt = driver.findElement(By.id("trigger"));
    URL url = new URL(elt.getAttribute("href"));

    //
    driver.get(url.toString());
    assertEquals("ok", driver.findElement(By.tagName("body")).getText());

    // Now we should get an application error
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    assertEquals(getErrorStatus(), conn.getResponseCode());
    driver.get(url.toString());
    assertEquals("java.lang.RuntimeException: throwed", driver.findElement(By.cssSelector("div.juzu > section > p")).getText().trim());

    // Make a change
    JavaFile pkgFile = getCompiler().assertSource("bridge", "runmode", "live", "A.java");
    pkgFile.assertSave(pkgFile.assertContent().replace("\"ok\"", "\"OK\""));

    //
    driver.get(applicationURL().toString());
    elt = driver.findElement(By.id("trigger"));
    elt.click();
    assertEquals("OK", driver.findElement(By.tagName("body")).getText());

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
