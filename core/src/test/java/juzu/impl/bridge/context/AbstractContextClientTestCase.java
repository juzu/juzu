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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractContextClientTestCase extends AbstractWebTestCase {

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
    // Little trick to force redirect after post
    client.setRedirectStrategy(new DefaultRedirectStrategy() {
      @Override
      protected boolean isRedirectable(String method) {
        return true;
      }
    });
    try {
      HttpPost post = new HttpPost(url.toURI());
      post.setEntity(new StringEntity("foo", ContentType.create("application/octet-stream", "UTF-8")));
      HttpResponse response = client.execute(post);
      assertEquals(200, response.getStatusLine().getStatusCode());
      assertEquals(3, contentLength);
      assertEquals("UTF-8", charset);
      assertEquals("application/octet-stream; charset=UTF-8", contentType);
      assertEquals("foo", content);
      assertEquals(kind, AbstractContextClientTestCase.kind);
    }
    finally {
      client.getConnectionManager().shutdown();
    }
  }
}
