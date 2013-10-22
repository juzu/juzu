/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.ajax;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import juzu.test.UserAgent;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractAjaxTestCase extends AbstractWebTestCase {

  protected static WebArchive createDeployment(WebArchive war) {
    URL jquery = AbstractWebTestCase.class.getResource("jquery-1.7.1.js");
    URL test = AbstractWebTestCase.class.getResource("test.js");
    URL stylesheet = AbstractWebTestCase.class.getResource("main.css");
    return war.
        addAsWebResource(jquery, "jquery.js").
        addAsWebResource(test, "test.js").
        addAsWebResource(stylesheet, "main.css");
  }

  @Test
  @RunAsClient
  public void testAjaxResource() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();

    HttpURLConnection conn = (HttpURLConnection)page.getUrl().openConnection();
    assertEquals(200, conn.getResponseCode());
    String s = Tools.read(conn.getInputStream());

    //
    HtmlAnchor trigger1 = (HtmlAnchor)page.getElementById("trigger1");
    trigger1.click();
    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("m1()"), alerts);

    //
    HtmlAnchor trigger2 = (HtmlAnchor)page.getElementById("trigger2");
    trigger2.click();
    alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("m1()", "m2(foo)"), alerts);

    //
    HtmlAnchor trigger3 = (HtmlAnchor)page.getElementById("trigger3");
    trigger3.click();
    alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("m1()", "m2(foo)", "m3()"), alerts);
  }
}
