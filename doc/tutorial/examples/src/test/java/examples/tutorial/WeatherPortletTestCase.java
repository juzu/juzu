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

import junit.framework.Assert;
import juzu.arquillian.BaseTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public class WeatherPortletTestCase extends BaseTest {

  @Deployment
  public static WebArchive deployment() {
    WebArchive war = createBasePortletDeployment();
    war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/portlet.xml"));
    war.addAsWebInfResource(new File("src/test/resources/spring.xml"));
    war.addPackages(true, "examples.tutorial");
    return war;
  }

  @ArquillianResource
  URL deploymentURL;

  @Drone
  WebDriver driver;

  @Test
  @RunAsClient
  public void testWeather1() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather1Portlet").toURL();
    driver.get(url.toString());
    Assert.assertTrue(driver.getPageSource().contains("The weather application"));
  }

  @Test
  @RunAsClient
  public void testWeather2() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather2Portlet").toURL();
    driver.get(url.toString());
    String source = driver.getPageSource();
    Assert.assertTrue(source.contains("temperature in marseille"));
    Assert.assertTrue(source.contains("20 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather3() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather3Portlet").toURL();
    driver.get(url.toString());
    String source = driver.getPageSource();
    Assert.assertTrue(source.contains("temperature in marseille"));
    Assert.assertTrue(source.contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather4() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather4Portlet").toURL();
    driver.get(url.toString());
    String source = driver.getPageSource();
    Assert.assertTrue(source.contains("temperature in marseille"));
    Assert.assertTrue(source.contains("10 degrees"));
    WebElement parisElt = driver.findElement(By.linkText("Paris"));
    parisElt.click();
    source = driver.getPageSource();
    Assert.assertTrue(source.contains("temperature in paris"));
    Assert.assertTrue(source.contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather5() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather5Portlet").toURL();
    driver.get(url.toString());
    WebElement locationElt = driver.findElement(By.cssSelector("input[name='location']"));
    locationElt.sendKeys("bastia");
    locationElt.submit();
    String source = driver.getPageSource();
    Assert.assertTrue(source.contains("temperature in bastia"));
    Assert.assertTrue(source.contains("10 degrees"));
  }

  @Test
  @RunAsClient
  public void testWeather7() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather7Portlet").toURL();
    driver.get(url.toString());
    WebElement link = driver.findElement(By.tagName("link"));
    String href = link.getAttribute("href");
    url = new URL(href);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    Assert.assertEquals(200, conn.getResponseCode());
  }

  @Test
  @RunAsClient
  public void testWeather8() throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/Weather8Portlet").toURL();
    driver.get(url.toString());
    WebElement p = new WebDriverWait(driver, 10).until(new ExpectedCondition<WebElement>() {
      public WebElement apply(WebDriver input) {
        return input.findElement(By.cssSelector("div.accordion-inner p"));
      }
    });
    Assert.assertTrue(p.getText().contains("temperature in marseille"));
    Assert.assertTrue(p.getText().contains("10 degrees"));
  }
}
