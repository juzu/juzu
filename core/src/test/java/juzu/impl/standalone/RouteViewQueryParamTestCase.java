package juzu.impl.standalone;

import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteViewQueryParamTestCase extends AbstractStandaloneTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testPathParam() throws Exception {
    assertDeploy("standalone", "route", "view", "queryparam");
    driver.get(deploymentURL.toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    String href = trigger.getAttribute("href");
    URL url = new URL(href);
    assertEquals("/juzu/foo", url.getPath());
    assertEquals("juu=bar", url.getQuery());
    trigger.click();
    String pass = driver.findElement(By.tagName("body")).getText();
    assertEquals("bar", pass);
    assertUndeploy();
  }
}
