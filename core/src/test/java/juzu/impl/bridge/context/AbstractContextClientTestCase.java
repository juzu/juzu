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

import junit.framework.Assert;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-type", "application/octet-stream;charset=UTF8");
    conn.connect();
    OutputStream out = conn.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
    writer.write("foo");
    writer.flush();
    Assert.assertEquals(200, conn.getResponseCode());
    assertEquals(3, contentLength);
    assertEquals("UTF8", charset);
    assertEquals("application/octet-stream;charset=UTF8", contentType);
    assertEquals("foo", content);
    assertEquals(kind, AbstractContextClientTestCase.kind);
  }
}
