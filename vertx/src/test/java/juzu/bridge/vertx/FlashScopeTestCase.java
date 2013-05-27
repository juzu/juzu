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

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@TestModule(
    name = "juzu-v1.0",
    jsonConfig = "{ \"main\":\"flashscope\"}")
public class FlashScopeTestCase extends VertxTestCase {

  /** . */
  public static String VALUE;

  @Test
  public void testFoo() throws Exception {

    //
    CookieManager manager = new CookieManager();
    manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(manager);

    VALUE = null;
    URL url = new URL("http://localhost:8080/");
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    Assert.assertNotNull(VALUE);
    String req1 = VALUE;

    // Same request
    VALUE = null;
    conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    Assert.assertEquals(req1, VALUE);

    // Same request
    VALUE = null;
    conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    String req2 = VALUE;
    Assert.assertNotSame(req1, req2);

    // Same request
    VALUE = null;
    conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    Assert.assertEquals(200, conn.getResponseCode());
    Assert.assertEquals(req2, VALUE);
  }
}
