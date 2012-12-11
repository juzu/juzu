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
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.InputStream;

/**
 * The base test provides helper method for setting up an Arquillian based test for Juzu.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class BaseTest {

  /**
   * Create a base portlet war file. The returned war file is configured for running the GateIn
   * embedded portlet container.
   *
   * @return the base portlet deployment
   */
  public static WebArchive createBasePortletDeployment() {
    WebArchive war = ShrinkWrap.create(WebArchive.class);

    // Embedded portlet container configuration
    InputStream in = BaseTest.class.getResourceAsStream("portlet/web.xml");
    if (in == null) {
      throw new AssertionError("Could not find web.xml for embedded portlet container");
    }
    war.setWebXML(new ByteArrayAsset(in));

    //
    return war;
  }

}
