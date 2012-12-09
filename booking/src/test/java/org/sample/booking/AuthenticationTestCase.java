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
import org.sample.booking.qualifier.Authentication;
import java.io.File;
import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/8/12
 */
@RunWith(Arquillian.class)
public class AuthenticationTestCase extends TestCase
{
   @Deployment
   public static WebArchive createDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "juzu.war");
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

   @Drone
   @Authentication
   WebDriver driver;

   @Test
   @InSequence(0)
   @RunAsClient
   public void login(@ArquillianResource URL deploymentURL) throws Exception
   {
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
   public void refreshAfterLogin(@ArquillianResource URL deploymentURL) throws Exception
   {
      URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
      driver.get(url.toString());
      WebElement options = driver.findElement(By.className("options"));
      assertNotNull(options);
      assertEquals("Connected as demo | Search | Settings | Logout", options.getText());
   }

   @Test
   @InSequence(2)
   @RunAsClient
   public void logout(@ArquillianResource URL deploymentURL) throws Exception
   {
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
   public void refreshAfterLogout(@ArquillianResource URL deploymentURL) throws Exception
   {
      URL url = deploymentURL.toURI().resolve("embed/BookingPortlet").toURL();
      driver.get(url.toString());

      WebElement login = driver.findElement(By.className("login"));
      assertNotNull(login);
      assertTrue(login.getText().contains("(try with demo/demo)"));
      assertNotNull(login.findElement(By.tagName("form")));
   }
}