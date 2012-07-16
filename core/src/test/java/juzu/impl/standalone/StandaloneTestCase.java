package juzu.impl.standalone;

import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class StandaloneTestCase extends AbstractStandaloneTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testRender() throws Exception {
    assertDeploy("standalone", "render");
    driver.get(deploymentURL.toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    String ok = driver.findElement(By.tagName("body")).getText();
    assertEquals("ok", ok);
  }
}
