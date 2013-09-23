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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import juzu.test.AbstractWebTestCase;
import juzu.test.UserAgent;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TransitivityTestCase extends AbstractWebTestCase {
  @Deployment
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.asset.transitivity");
  }

  @Test
  @RunAsClient
  public void testSatisfied() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();
    List<String> alerts = ua.getAlerts(page);
    assertEquals(Arrays.asList("bar", "foo"), alerts);
  }
}
