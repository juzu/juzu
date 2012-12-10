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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.List;
import static junit.framework.Assert.*;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/9/12
 */
public class BookingTestCase extends AbstractBookingTestCase {

  public static final int WAIT_TIME = 20;

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
    loginForm.findElement(By.name("username")).sendKeys("demo");
    loginForm.findElement(By.name("password")).sendKeys("demo");
    loginForm.submit();

    WebElement messageBoard = driver.findElement(By.className("fSuccess"));
    assertNotNull(messageBoard);
    assertEquals("Welcome, Demo User", messageBoard.getText());

    //refresh page and wait until JavaScript code is loaded
    driver.get(url.toString());
  }

  @Test
  @InSequence(1)
  @RunAsClient
  public void searchHotels() throws Exception {
    WebElement findHotels = driver.findElement(By.className("submit"));
    assertNotNull(findHotels);
    findHotels.click();

    //
    WebElement tbody = new WebDriverWait(driver, 10).until(new ExpectedCondition<WebElement>() {
      public WebElement apply(WebDriver input) {
        return input.findElement(By.cssSelector(".result tbody"));
      }
    });

    //
    List<WebElement> hotels = tbody.findElements(By.tagName("tr"));
    assertEquals(5, hotels.size());

    String[] expectedHotels = {"Marriott Courtyard", "Doubletree", "Hotel Rouge", "70 Park Avenue Hotel", "Conrad Miami"};
    for (int i = 0;i < hotels.size();i++) {
      assertEquals(expectedHotels[i], hotels.get(i).findElements(By.tagName("td")).get(0).getText());
    }
  }

  @Test
  @InSequence(2)
  @RunAsClient
  public void viewHotel() throws Exception {
    //View the hotel Marriott Courtyard
    WebElement marriottHotel = driver.findElement(By.className("result")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).get(0);
    assertNotNull(marriottHotel);

    List<WebElement> hotelInfos = marriottHotel.findElements(By.tagName("td"));
    assertEquals("Marriott Courtyard", hotelInfos.get(0).getText());
    assertEquals("Tower Place, Buckhead", hotelInfos.get(1).getText());
    assertEquals("Atlanta, GA, USA", hotelInfos.get(2).getText());
    assertEquals("30305", hotelInfos.get(3).getText());
    assertEquals("View Hotel", hotelInfos.get(4).getText());

    WebElement viewHotel = hotelInfos.get(4).findElement(By.linkText("View Hotel"));
    viewHotel.click();
  }

  @Test
  @InSequence(3)
  @RunAsClient
  public void bookHotel() throws Exception {
    //Book the hotel Marriott Courtyard
    WebElement bookLink = driver.findElement(By.className("buttons")).findElement(By.linkText("Book Hotel"));
    assertNotNull(bookLink);
    bookLink.click();
    WebElement bookForm = driver.findElement(By.tagName("form"));
    assertNotNull(bookForm);
    bookForm.findElement(By.className("buttons")).findElement(By.name("confirm")).click();
  }

  @Test
  @InSequence(4)
  @RunAsClient
  public void checkBooking() throws Exception {
    WebElement success = driver.findElement(By.className("fSuccess"));
    assertNotNull(success);
    assertNotNull("Thank you, demo, your confimation number for Marriott Courtyard is 0", success.findElement(By.tagName("strong")).getText());
  }
}
