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

package juzu.impl.bridge.servlet;

import juzu.HttpMethod;
import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteRequestHandlerResourceTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "bridge.servlet.request.method.resource");
  }

  @Test
  public void testMethods() throws Exception {
    HttpMethod[] methods = { HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE };
    boolean[] doOutput = { false, true, true, true };
    URL url = applicationURL();
    for (int i = 0;i < methods.length;i++) {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setDoOutput(doOutput[i]);
      HttpMethod method = methods[i];
      conn.setRequestMethod(method.name());
      assertEquals(200, conn.getResponseCode());
      String ret = Tools.read(conn.getInputStream());
      assertTrue("Was expecting " + ret + " to container ok[" + method + "]", ret.contains("ok[" + method + "]"));
    }
  }
}
