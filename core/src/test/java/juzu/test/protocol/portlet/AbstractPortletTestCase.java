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

package juzu.test.protocol.portlet;

import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Formatter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractPortletTestCase extends AbstractWebTestCase {

  public static WebArchive createPortletDeployment(String packageName) {
    return createPortletDeployment(false, packageName);
  }

  public static WebArchive createPortletDeployment(boolean incremental, String packageName) {

    //
    WebArchive war = createDeployment(true, incremental, packageName);

    //
    String runModeValue;
    String sourcePath;
    try {
      runModeValue = incremental ? "dev" : "prod";
      sourcePath = incremental ? getCompiler().getSourcePath().getRoot().getCanonicalFile().getAbsolutePath() : "";
    }
    catch (IOException e) {
      throw failure("Could not read obtain source path", e);
    }

    // Descriptor
    String portlet;
    try {
      portlet = Tools.read(AbstractPortletTestCase.class.getResourceAsStream("portlet.xml"));
    }
    catch (IOException e) {
      throw failure("Could not read portlet xml deployment descriptor", e);
    }
    portlet = String.format(
        portlet,
        "weld",
        runModeValue,
        sourcePath);

    //
    war.setWebXML(AbstractPortletTestCase.class.getResource("web.xml"));
    war.addAsWebInfResource(new StringAsset(portlet), "portlet.xml");

    // Add libraries we need
/*
    war.addAsLibraries(DependencyResolvers.
        use(MavenDependencyResolver.class).
        loadEffectivePom("pom.xml")
        .artifacts("javax.servlet:jstl", "taglibs:standard").
            resolveAsFiles());
*/

    //
    return war;
  }
}
