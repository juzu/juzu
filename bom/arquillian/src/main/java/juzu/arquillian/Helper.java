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

package juzu.arquillian;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An helper class that provides helper methods for setting up an Arquillian based test for Juzu.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Helper {

  /**
   * Create a base servlet war file. The returned war file is configured for running the Juzu
   * servlet with Weld container.
   *
   * @return the base servlet deployment
   */
  public static WebArchive createBaseServletDeployment() {
    return createBaseServletDeployment("weld");
  }

  /**
   * Create a base servlet war file. The returned war file is configured for running the Juzu
   * servlet and the specified injector provider.
   *
   * @param injectorVendor the injector vendor
   * @return the base servlet deployment
   */
  public static WebArchive createBaseServletDeployment(String injectorVendor) {
    return createBaseDeployment("servlet/web.xml", injectorVendor);
  }

  /**
   * Create a base portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container.
   *
   * @return the base portlet deployment
   */
  public static WebArchive createBasePortletDeployment() {
    return createBaseDeployment("portlet/web.xml", "weld");
  }

  private static WebArchive createBaseDeployment(String webXMLPath, String injectorProvider) {
    WebArchive war = ShrinkWrap.create(WebArchive.class);
    byte[] buffer = new byte[512];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InputStream in = Helper.class.getResourceAsStream(webXMLPath);
    if (in == null) {
      throw new AssertionError("Could not locate " + webXMLPath + " web.xml for juzu testing");
    }
    try {
      for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
        baos.write(buffer, 0, l);
      }
    }
    catch (IOException e) {
      throw new AssertionError("Could not find read " + webXMLPath + " web.xml for juzu testing");
    }
    finally {
      try {
        in.close();
      }
      catch (Throwable ignore) {
      }
    }
    String webXML = baos.toString();
    webXML = String.format(webXML, injectorProvider);
    war.setWebXML(new StringAsset(webXML));
    return war;
  }
}
