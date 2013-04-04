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

package org.sample.booking;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import static junit.framework.Assert.*;

import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/8/12
 */
public class AuthenticationTestCase extends AbstractBookingTestCase {

  @Deployment
  public static WebArchive createDeployment() {
    return AbstractBookingTestCase.createDeployment();
  }

  @Test
  @InSequence(0)
  @RunAsClient
  public void login(@ArquillianResource URL deploymentURL) throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
    driver.get(url.toString());
    WebElement loginForm = driver.findElement(By.className("formLogin"));
    assertNotNull(loginForm);

    loginForm.findElement(By.name("username")).sendKeys("demo");
    loginForm.findElement(By.name("password")).sendKeys("demo");
    loginForm.submit();

    WebElement messageBoard = driver.findElement(By.className("fSuccess"));
    assertNotNull(messageBoard);
    assertEquals("Welcome, Demo User", messageBoard.getText());
  }

  @Test
  @InSequence(1)
  @RunAsClient
  public void refreshAfterLogin(@ArquillianResource URL deploymentURL) throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
    driver.get(url.toString());
    WebElement options = driver.findElement(By.className("options"));
    assertNotNull(options);
    assertEquals("Connected as demo | Search | Settings | Logout", options.getText());
  }

  @Test
  @InSequence(2)
  @RunAsClient
  public void logout(@ArquillianResource URL deploymentURL) throws Exception {
    WebElement options = driver.findElement(By.className("options"));
    assertNotNull(options);
    assertEquals("Connected as demo | Search | Settings | Logout", options.getText());

    WebElement logout = options.findElement(By.linkText("Logout"));
    assertNotNull(logout);
    assertEquals("Logout", logout.getText());
    driver.get(logout.getAttribute("href"));

    WebElement login = driver.findElement(By.className("login"));
    assertNotNull(login);
    assertTrue(login.getText().contains("(try with demo/demo)"));
    assertNotNull(login.findElement(By.tagName("form")));
  }

  @Test
  @InSequence(3)
  @RunAsClient
  public void refreshAfterLogout(@ArquillianResource URL deploymentURL) throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
    driver.get(url.toString());

    WebElement login = driver.findElement(By.className("login"));
    assertNotNull(login);
    assertTrue(login.getText().contains("(try with demo/demo)"));
    assertNotNull(login.findElement(By.tagName("form")));
  }
}