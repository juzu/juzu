package juzu.impl.standalone;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.UserAgent;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.junit.Test;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class StandaloneTestCase extends AbstractStandaloneTestCase {

  @Test
  public void testRender() throws Exception {
    assertDeploy("standalone", "render");
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();
    HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
    page = trigger.click();
    assertEquals("ok", page.asText());
  }
}
