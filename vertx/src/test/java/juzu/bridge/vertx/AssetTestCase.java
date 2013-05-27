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

package juzu.bridge.vertx;

import junit.framework.Assert;
import juzu.impl.common.Tools;
import org.junit.Test;
import org.vertx.java.test.TestModule;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@TestModule(
    name = "juzu-v1.0",
    jsonConfig = "{ \"main\":\"asset\"}")
public class AssetTestCase extends VertxTestCase {

  @Test
  public void testFoo() throws Exception {
    URL url = new URL("http://localhost:8080/");
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    String result = Tools.read(conn.getInputStream());
    Assert.assertTrue(result.contains("src=\"/asset/assets/test.js\""));
    Assert.assertTrue(result.contains("src=\"http://localhost:8080/foo.js\""));
    Assert.assertEquals("function relative() {}", assertResource(url, "/asset/assets/test.js", "application/javascript"));
    Assert.assertEquals("function absolute() {}", assertResource(url, "/asset/absolute/script.js", "application/javascript"));
    Assert.assertNotNull(assertResource(url, "/asset/juzu/impl/plugin/ajax/script.js", "application/javascript"));
    Assert.assertEquals("content_value", assertResource(url, "/asset/assets/content.txt", "text/plain"));
  }

  private String assertResource(URL baseURL, String path, String expectedMimeType) throws Exception {
    HttpURLConnection conn = (HttpURLConnection)new URL(baseURL, path).openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    Map<String, String> headers = Tools.responseHeaders(conn);
    String contentType = headers.get("Content-Type");
    if (contentType == null) {
      // For some reason vertx change header to lower case....
      // until we figure out we need this
      contentType = headers.get("content-type");
    }
    Assert.assertNotNull(contentType);
    Assert.assertTrue("Was expecting " + contentType + " to start with " + expectedMimeType, contentType.startsWith(expectedMimeType));
    return Tools.read(conn.getInputStream());
  }

}
