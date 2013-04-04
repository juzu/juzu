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

package juzu.impl.plugin.asset;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.AbstractWebTestCase;
import juzu.test.UserAgent;
import juzu.test.protocol.http.HttpServletImpl;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractLocationTestCase extends AbstractWebTestCase {

  public static WebArchive createLocationDeployment(String applicationName) {
    WebArchive war = createServletDeployment(true, applicationName);
    URL jquery = HttpServletImpl.class.getResource("jquery-1.7.1.js");
    URL test = HttpServletImpl.class.getResource("test.js");
    URL stylesheet = HttpServletImpl.class.getResource("main.css");
    return war.
        addAsWebResource(jquery, "jquery.js").
        addAsWebResource(test, "test.js").
        addAsWebResource(stylesheet, "main.css");
  }

  @Test
  @RunAsClient
  public final void testSatisfied() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();

    // Script
    HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
    trigger.click();
    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("OK MEN"), alerts);

    // CSS
    DomNodeList<HtmlElement> links = page.getElementsByTagName("link");
    assertEquals(1, links.size());
    HtmlLink link = (HtmlLink)links.get(0);
    assertTrue(link.getHrefAttribute().endsWith("main.css"));
  }
}
