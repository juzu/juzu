package juzu.impl.plugin.asset;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.protocol.http.AbstractHttpTestCase;
import juzu.test.UserAgent;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ImplicitTestCase extends AbstractHttpTestCase {

  @Test
  public void testSatisfied() throws Exception {
    MockApplication<?> app = assertDeploy("plugin", "asset", "implicit");

    //
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();

    // Script
    HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
    trigger.click();
    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("OK MEN"), alerts);
  }
}
