/*
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
package juzu.impl.plugin.amd;

import java.net.URL;
import java.util.ArrayList;

import juzu.impl.common.Tools;
import juzu.test.UserAgent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 * Use mixin @Require and @Define
 */
public class AMDMixTestCase extends AbstractAMDTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(true, "plugin.amd.mix");
    return war;
  }

  @Test
  @RunAsClient
  public void test() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();
    ua.waitForBackgroundJavaScript(1000);
    DomNodeList<DomElement> scripts = page.getElementsByTagName("script");
    
    assertEquals(6, scripts.size());
    
    ArrayList<String> sources = new ArrayList<String>();
    for(DomElement script : scripts) {
      String src = script.getAttribute("src");
      if (src != null && !src.isEmpty()) {
        sources.add(src);
      }
    }
    
    assertList(Tools.list("/juzu/assets/juzu/impl/plugin/amd/require.js",
        "/juzu/assets/juzu/impl/plugin/amd/wrapper.js",
        "/juzu/assets/plugin/amd/mix/assets/bar.js",
        "/juzu/assets/plugin/amd/mix/assets/foo.js"), sources);
    
    String foo = Tools.read(new URL("http://localhost:" + getContainerPort() + "/juzu/assets/plugin/amd/mix/assets/foo.js")).trim();
    URL fooURL = Thread.currentThread().getContextClassLoader().getResource("plugin/amd/mix/assets/foo.js");
    assertEquals(Tools.read(fooURL), foo);
    
    String bar = Tools.read(new URL("http://localhost:" + getContainerPort() + "/juzu/assets/plugin/amd/mix/assets/bar.js")).trim();
    assertTrue(bar.startsWith("define('Bar', ['Foo'], function(foo) {"));
  }
}
