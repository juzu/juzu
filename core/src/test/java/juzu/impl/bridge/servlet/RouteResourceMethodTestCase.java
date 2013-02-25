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

package juzu.impl.bridge.servlet;

import juzu.Method;
import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteResourceMethodTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "bridge.servlet.route.resource.method");
  }

  @Test
  public void testMethods() throws Exception {
    Method[] methods = { Method.GET, Method.POST, Method.PUT, Method.DELETE };
    boolean[] doOutput = { false, true, true, true };
    URL url = applicationURL();
    for (int i = 0;i < methods.length;i++) {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setDoOutput(doOutput[i]);
      Method method = methods[i];
      conn.setRequestMethod(method.name());
      assertEquals(200, conn.getResponseCode());
      String ret = Tools.read(conn.getInputStream());
      assertTrue("Was expecting " + ret + " to container ok[" + method + "]", ret.contains("ok[" + method + "]"));
    }
  }
}
