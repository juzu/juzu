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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.AbstractWebTestCase;
import juzu.test.UserAgent;
import juzu.test.protocol.servlet.JuzuServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MimeTypeTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL jquery = JuzuServlet.class.getResource("jquery-1.7.1.js");
    URL css = JuzuServlet.class.getResource("main.css");
    URL less = JuzuServlet.class.getResource("main.less");
    return createServletDeployment(true, "plugin.asset.mimetype").
        addAsWebResource(jquery, "jquery.js").
        addAsWebResource(css, "main.css").
        addAsWebResource(css, "main.less");
  }

  @Test
  public void testSatisfied() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();

    // Script
    HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
    trigger.click();
    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("OK MEN"), alerts);

    // CSS
    DomNodeList<DomElement> links = page.getElementsByTagName("link");
    assertEquals(2, links.size());
    HtmlLink link1 = (HtmlLink)links.get(0);
    assertEquals("stylesheet", link1.getRelAttribute());
    assertEquals("/juzu/main.css", link1.getHrefAttribute());
    assertEquals("text/css", link1.getTypeAttribute());
    HtmlLink link2 = (HtmlLink)links.get(1);
    assertEquals("stylesheet", link2.getRelAttribute());
    assertEquals("/juzu/main.less", link2.getHrefAttribute());
    assertEquals("text/less", link2.getTypeAttribute());
  }
}
