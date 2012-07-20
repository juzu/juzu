package juzu.impl.standalone;

import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractRouteBodyParamTestCase extends AbstractStandaloneTestCase {

  @Drone
  WebDriver driver;

  protected abstract String[] getApplication();

  @Test
  public void testPathParam() throws Exception {
    assertDeploy(getApplication());
    driver.get(deploymentURL.toString());
    WebElement form = driver.findElement(By.id("form"));
    String action = form.getAttribute("action");
    URL url = new URL(action);
    assertEquals("/juzu/foo", url.getPath());
    assertNull(url.getQuery());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    String pass = driver.findElement(By.tagName("body")).getText();
    assertEquals("bar", pass);
    assertUndeploy();
  }
}
