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

package juzu.impl.bridge.context;

import juzu.impl.common.Tools;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ContextApplicationPortletTestCase extends AbstractContextApplicationTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {
    return createDeployment("bridge.context.application");
  }

  public static WebArchive createDeployment(String packageName) throws IOException {
    WebArchive war = createPortletDeployment(packageName);
    Node node = war.get("WEB-INF/portlet.xml");
    ArchivePath path = node.getPath();
    String s = Tools.read(node.getAsset().openStream(), Tools.UTF_8);
    s = s.replace("<portlet-info>", "<resource-bundle>bundle</resource-bundle>" + "<portlet-info>");
    war.delete(path);
    war.add(new StringAsset(s), path);
    war.addAsResource(new StringAsset("abc=def"), "bundle_fr_FR.properties");
    return war;
  }

  @Test
  public void testBundle() throws Exception {
    test(getPortletURL());
  }
}
