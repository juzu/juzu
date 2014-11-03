/*
 * Copyright 2014 eXo Platform SAS
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

package org.juzu.tutorial.juzcret.step7;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JuZcretTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createPortletDeployment("org.juzu.tutorial.juzcret.step7");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testRender() throws Exception {
    driver.get(getPortletURL().toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertTrue(body.getText().indexOf("JuZcret Portlet") != -1);
    System.out.println(driver.getPageSource());
  }

  @Test
  public void testSecret() throws Exception {
    driver.get(getPortletURL().toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertFalse(body.getText().contains("test secret text"));

    // add secret form
    WebElement shareBtn = driver.findElement(By.cssSelector(".secret-wall-heading a"));
    driver.get(shareBtn.getAttribute("href"));
    // input
    WebElement secretInput = driver.findElement(By.tagName("textarea"));
    secretInput.sendKeys("test secret text");
    // submit
    WebElement submitBtn = driver.findElement(By.tagName("button"));
    submitBtn.click();

    // wait for redirecting to index page
    body = new WebDriverWait(driver, 10).until(new ExpectedCondition<WebElement>() {
      public WebElement apply(WebDriver drv) {
        return drv.findElement(By.tagName("body"));
      }
    });
    assertTrue(body.getText().contains("test secret text"));
  }
  
  @Test
  public void testAsset() throws Exception {
    driver.get(getPortletURL().toString());
    
    List<WebElement> scripts = driver.findElements(By.tagName("script"));
    Set<String> srcScripts = new HashSet<String>();
    for (WebElement elem : scripts) {
      srcScripts.add(elem.getAttribute("src"));
    }
    assertTrue(srcScripts.contains("http://localhost:8080/juzu/assets/org/juzu/tutorial/juzcret/step7/assets/jquery/1.10.2/jquery.js"));
    assertTrue(srcScripts.contains("http://localhost:8080/juzu/assets/juzu/impl/plugin/ajax/script.js"));
    assertTrue(srcScripts.contains("http://localhost:8080/juzu/assets/org/juzu/tutorial/juzcret/step7/assets/javascripts/secret.js"));
    
    WebElement style = driver.findElement(By.tagName("link"));
    assertEquals("http://localhost:8080/juzu/assets/org/juzu/tutorial/juzcret/step7/assets/styles/juzcret.css",
                   style.getAttribute("href"));
  }

  @Test
  public void testLike() throws Exception {
    driver.get(getPortletURL().toString());

    // like
    WebElement likeBtn = driver.findElement(By.cssSelector(".btn-like"));
    likeBtn.click();

    // wait
    By selector = By.cssSelector(".btn-like .numb");
    ExpectedCondition<Boolean> condition = ExpectedConditions.textToBePresentInElement(selector, "1");
    assertTrue(new WebDriverWait(driver, 10).until(condition));
  }

  @Test
  public void testComment() throws Exception {
    driver.get(getPortletURL().toString());
    WebElement body = driver.findElement(By.tagName("body"));
    assertFalse(body.getText().contains("test comment"));

    // input
    WebElement commentInput = driver.findElement(By.cssSelector(".secret-add-comment"));
    commentInput.sendKeys("test comment");
    // submit
    WebElement submitBtn = driver.findElement(By.cssSelector(".btn-comment"));
    submitBtn.click();
    // wait
    ExpectedCondition<Boolean> condition = ExpectedConditions.textToBePresentInElement(By.cssSelector(".secr-comments-list"),
                                                                                       "test comment");
    assertTrue(new WebDriverWait(driver, 10).until(condition));
  }
}
