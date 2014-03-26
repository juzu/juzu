/*
 * Copyright (C) 2003-2014 eXo Platform SAS. 
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
package plugin.shiro.authc;

import java.io.IOException;

import juzu.impl.inject.spi.InjectorProvider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.SimpleRealm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 27, 2014
 */
public class RememberMeTestCase extends AbstractShiroTestCase {
  @Drone
  WebDriver driver;
  
  public static Exception exception;
  
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {
    WebArchive war = createServletDeployment(InjectorProvider.SPRING, "plugin.shiro.authc.remember");
    war.addPackages(true, SimpleRealm.class.getPackage());
    return war;
  }

  @Test
  @InSequence(0)
  @RunAsClient
  public void testLoginSuccess() throws Exception {
    driver.get(deploymentURL.toString());
    WebElement username = driver.findElement(By.id("uname"));
    username.sendKeys("root");
    WebElement password = driver.findElement(By.id("passwd"));
    password.sendKeys("secret");
    WebElement remember = driver.findElement(By.id("remember"));
    remember.click();
    WebElement submit = driver.findElement(By.id("submit"));
    submit.click();
    
    assertNull(exception);
    waitForPresent(driver, "root logged");
    
    org.openqa.selenium.Cookie cookie = driver.manage().getCookieNamed("rememberMe");
    assertNotNull(cookie);
  }
  
  @Test
  @InSequence(1)
  @RunAsClient
  public void testAfterLogin() throws Exception {
    driver.get(deploymentURL.toString());
    waitForPresent(driver, "root logged");
    
    String rememberToken = driver.manage().getCookieNamed("rememberMe").getValue();
    
    driver.manage().deleteAllCookies();
    
    driver.get(deploymentURL.toString());
    WebElement username = driver.findElement(By.id("uname"));
    assertNotNull(username);
    
    driver.manage().addCookie(new Cookie("rememberMe", rememberToken));
    
    driver.get(deploymentURL.toString());
    waitForPresent(driver, "root logged");
  }
}
