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

package juzu.impl.bridge.response;

import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:benjamin.paillereau@exoplatform.com">Benjamin Paillereau</a> */
public abstract class AbstractResponseCharsetResourceTestCase extends AbstractWebTestCase {

  /** . */
  public static Charset charset;

  /** . */
  public static String url;

  @Drone
  WebDriver driver;

  @Test
  public void testPathParam() throws Exception {
    testUTF_8();
    testISO_8859_1();
  }

  protected void testUTF_8() throws Exception {
    testWith(EURO, Tools.UTF_8);
  }

  protected void testISO_8859_1() throws Exception {
    testWith("", Tools.ISO_8859_1);
  }

  private void testWith(String expected, Charset charset) throws Exception {
    driver.get(applicationURL().toString());
    AbstractResponseCharsetResourceTestCase.charset = charset;
    URL url = new URL(AbstractResponseCharsetResourceTestCase.url);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    assertEquals(200, conn.getResponseCode());
    Map<String, String> headers = Tools.responseHeaders(conn);
    String contentType = headers.get("Content-Type");
    assertNotNull(contentType);
    Pattern p2 = Pattern.compile("^text/html;charset=(.*)$");
    Matcher m2 = p2.matcher(contentType);
    assertTrue(m2.matches());
    assertEquals(charset, Charset.forName(m2.group(1)));
    String s = Tools.read(conn.getInputStream(), charset);
    Pattern p = Pattern.compile("content\\[(.*)\\]");
    Matcher m = p.matcher(s);
    assertTrue(m.find());
    assertEquals(expected, m.group(1));
  }
}
