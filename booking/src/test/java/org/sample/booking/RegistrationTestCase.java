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

import junit.framework.TestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sample.booking.qualifier.Registration;
import java.io.File;
import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/9/12
 */
@RunWith(Arquillian.class)
public class RegistrationTestCase extends TestCase
{

   @Drone
   @Registration
   WebDriver driver;

   @Deployment
   public static WebArchive createDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "booking.war");
      war.setWebXML("org/sample/booking/web.xml");
      war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/portlet.xml"));
      war.addAsWebResource(new File("src/main/webapp/public/javascripts/jquery-1.7.1.min.js"),
         "public/javascripts/jquery-1.7.1.min.js");
      war.addAsWebResource(new File("src/main/webapp/public/javascripts/jquery-ui-1.7.2.custom.min.js"),
         "public/javascripts/jquery-ui-1.7.2.custom.min.js");
      war.addAsWebResource(new File("src/main/webapp/public/javascripts/booking.js"), "public/javascripts/booking.js");
      war.addAsWebResource(new File("src/main/webapp/public/stylesheets/main.css"), "public/stylesheets/main.css");
      war.addAsWebResource(new File("src/main/webapp/public/ui-lightness/jquery-ui-1.7.2.custom.css"),
         "public/ui-lightness/jquery-ui-1.7.2.custom.css");

      return war;
   }

   @Test
   @InSequence(0)
   @RunAsClient
   public void registerUser(@ArquillianResource URL deploymentURL) throws Exception
   {
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
