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
