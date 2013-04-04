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
 * @date 11/9/12
 */
public class RegistrationTestCase extends AbstractBookingTestCase {

  @Deployment
  public static WebArchive createDeployment() {
    return AbstractBookingTestCase.createDeployment();
  }

  @Test
  @InSequence(0)
  @RunAsClient
  public void registerUser(@ArquillianResource URL deploymentURL) throws Exception {
    URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
    driver.get(url.toString());
    WebElement registerLink = driver.findElement(By.linkText("Register New User"));
    assertNotNull(registerLink);
    registerLink.click();
    WebElement registerForm = driver.findElement(By.tagName("form"));
    assertNotNull(registerForm);

    registerForm.findElement(By.name("username")).sendKeys("testUser");
    registerForm.findElement(By.name("name")).sendKeys("testUserName");
    registerForm.findElement(By.name("password")).sendKeys("testUserPassword");
    registerForm.findElement(By.name("verifyPassword")).sendKeys("testUserPassword");
    registerForm.submit();

    WebElement messageBoard = driver.findElement(By.className("fSuccess"));
    assertNotNull(messageBoard);
    assertEquals("Welcome, testUserName", messageBoard.getText());
  }
}
