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

package juzu.impl.bridge.context;

import juzu.impl.common.Tools;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletApplicationContextTestCase extends AbstractApplicationContextTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {
    WebArchive war = createPortletDeployment("bridge.context.application");
    Node node = war.get("WEB-INF/portlet.xml");
    ArchivePath path = node.getPath();
    String s = Tools.read(node.getAsset().openStream(), "UTF-8");
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
