package juzu.impl.standalone;

import juzu.impl.common.Tools;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.net.HttpURLConnection;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResponseHeaderViewTestCase extends AbstractStandaloneTestCase {

  @Drone
  WebDriver driver;

  @Test
  public void testPathParam() throws Exception {
    assertDeploy("standalone", "response", "header", "view");
    HttpURLConnection conn = (HttpURLConnection)deploymentURL.openConnection();
    conn.connect();
    Map<String, String> headers = Tools.responseHeaders(conn);
    assertTrue(headers.containsKey("foo"));
    assertEquals("bar", headers.get("foo"));
    assertUndeploy();
  }
}
