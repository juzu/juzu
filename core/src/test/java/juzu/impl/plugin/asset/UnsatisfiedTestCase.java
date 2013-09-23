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

import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class UnsatisfiedTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.asset.unsatisfied");
  }

  @Test
  public void testWhat() throws Exception {
    HttpURLConnection conn = (HttpURLConnection)applicationURL().openConnection();
    conn.connect();
    assertEquals(500, conn.getResponseCode());
    String ret = Tools.read(conn.getErrorStream());
    assertTrue(ret.contains("Cannot resolve asset"));
    assertTrue(ret.contains("foo"));
  }
}
