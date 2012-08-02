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

package juzu.impl.plugin.ajax;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.protocol.http.AbstractHttpTestCase;
import juzu.test.UserAgent;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxTestCase extends AbstractHttpTestCase {

  @Test
  public void testAjaxResource() throws Exception {
    MockApplication<?> app = assertDeploy("plugin", "ajax");

    //
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();

    System.out.println("page.asText() = " + page.asText());

    HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
    trigger.click();

    HtmlAnchor trigger2 = (HtmlAnchor)page.getElementById("trigger2");
    trigger2.click();

    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("OK MEN", "OK MEN 2"), alerts);
  }
}
