package juzu.impl.portlet;

import juzu.impl.common.Tools;
import juzu.test.protocol.portlet.AbstractPortletTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.portletbridge.arquillian.enrichers.resource.PlutoURLProvider;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionTestCase extends AbstractPortletTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testFoo() throws Exception {
    assertDeploy("portlet", "action");
    URL portalURL = new PlutoURLProvider().customizeURL(deploymentURL);
    driver.get(portalURL.toString());
    WebElement trigger = driver.findElement(By.id("trigger"));
    trigger.click();
    WebElement body = driver.findElement(By.tagName("body"));
    assertEquals(1, Tools.count(body.getText(), "pass"));
    assertUndeploy();
  }
}
