/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.plugin.asset;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.UserAgent;
import juzu.test.protocol.http.HttpServletImpl;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractLocationTestCase extends AbstractStandaloneTestCase {

  public static WebArchive createLocationDeployment(String applicationName) {
    WebArchive war = AbstractStandaloneTestCase.createDeployment(applicationName);
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
  }
}
