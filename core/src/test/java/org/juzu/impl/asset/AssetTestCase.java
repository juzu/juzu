package org.juzu.impl.asset;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.juzu.test.AbstractHttpTestCase;
import org.juzu.test.UserAgent;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetTestCase extends AbstractHttpTestCase
{

   @Test
   public void testPlugin() throws Exception
   {
      assertDeploy("asset", "plugin");

      //
      UserAgent ua = assertInitialPage();
      String url = ua.getHomePage().asText();
      HtmlPage page = ua.getPage(url);
      assertEquals("foo", page.asText());
   }

   @Test
   public void testApplication() throws Exception
   {
      assertDeploy("asset", "application");

      //
      UserAgent ua = assertInitialPage();
      String url = ua.getHomePage().asText();
      HtmlPage page = ua.getPage(url);
      assertEquals("foo", page.asText());
   }
}
