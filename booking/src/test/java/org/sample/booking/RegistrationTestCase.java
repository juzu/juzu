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
