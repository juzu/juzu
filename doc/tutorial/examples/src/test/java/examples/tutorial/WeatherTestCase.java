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

package examples.tutorial;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class WeatherTestCase {

  @ArquillianResource
  URL deploymentURL;

  @Drone
  WebDriver driver;

  public abstract URL getApplicationURL(String application);

  @Test
  @RunAsClient
  public void testWeather1() throws Exception {
    URL url = getApplicationURL("weather1"); // <1> Retrieve the application
    driver.get(url.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("The weather application")); // <2> Check markup is correct
  }

  @Test
  @RunAsClient
  public void testWeather2() throws Exception {
    URL url = getApplicationURL("weather2");
    driver.get(url.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("temperature in marseille"));
    assertTrue(body.getText().contains("20 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather3() throws Exception {
    URL url = getApplicationURL("weather3");
    driver.get(url.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("temperature in marseille"));
    assertTrue(body.getText().contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather4() throws Exception {
    URL url = getApplicationURL("weather4");
    driver.get(url.toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("temperature in marseille"));
    assertTrue("Was expecting to find <10 degrees> in " + body.getText(), body.getText().contains("10 degrees"));
    WebElement parisElt = driver.
        findElement(By.linkText("Paris"));
    parisElt.click();
    body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("temperature in paris"));
    assertTrue(body.getText().contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather5() throws Exception {
    URL url = getApplicationURL("weather5");
    driver.get(url.toString());
    WebElement locationElt = driver.
        findElement(By.cssSelector("input[name='location']"));
    locationElt.sendKeys("bastia");
    locationElt.submit();
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().contains("temperature in bastia"));
    assertTrue(body.getText().contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather7() throws Exception {
    URL url = getApplicationURL("weather7");
    driver.get(url.toString());
    WebElement link = driver.findElement(By.tagName("link"));
    String href = link.getAttribute("href");
    url = new URL(href);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    assertEquals(200, conn.getResponseCode());
  }

  @Test
  @RunAsClient
  public void testWeather8() throws Exception {
    URL url = getApplicationURL("weather8");
    driver.get(url.toString());
    WebElement p = new WebDriverWait(driver, 10).until(
      new ExpectedCondition<WebElement>() {
        public WebElement apply(WebDriver input) {
          return input.findElement(
              By.cssSelector("div.accordion-inner p"));
        }
    });
    assertTrue(p.getText().contains("temperature in marseille"));
    assertTrue(p.getText().contains("10 degrees"));
  }
}
