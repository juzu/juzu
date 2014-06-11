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

import juzu.test.AbstractWebTestCase;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractClientContextTestCase extends AbstractWebTestCase {

  /** . */
  public static String kind;

  /** . */
  public static String contentType;

  /** . */
  public static String charset;

  /** . */
  public static String content;

  /** . */
  public static int contentLength;

  @Drone
  WebDriver driver;

  protected void test(URL initialURL, String kind) throws Exception {
    driver.get(initialURL.toString());
    WebElement link = driver.findElement(By.id(kind));
    contentLength = -1;
    charset = null;
    contentType = null;
    content = null;
    URL url = new URL(link.getAttribute("href"));

    DefaultHttpClient client = new DefaultHttpClient();
    client.setRedirectStrategy(new DefaultRedirectStrategy() {                
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
            boolean isRedirect=false;
            try {
                isRedirect = super.isRedirected(request, response, context);
            } catch (ProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!isRedirect) {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 301 || responseCode == 302) {
                    return true;
                }
            }
            return isRedirect;
        }});

    try {
        HttpPost post = new HttpPost(url.toURI());
        post.setEntity(new StringEntity("foo", HTTP.OCTET_STREAM_TYPE, HTTP.UTF_8));
        HttpResponse response = client.execute(post);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(3, contentLength);
        assertEquals("UTF-8", charset);
        assertEquals("application/octet-stream; charset=UTF-8", contentType);
        assertEquals("foo", content);
        assertEquals(kind, AbstractClientContextTestCase.kind);
    } finally {
        client.getConnectionManager().shutdown();
    }
  }
}
