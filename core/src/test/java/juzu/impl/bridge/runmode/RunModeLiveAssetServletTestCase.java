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

package juzu.impl.bridge.runmode;

import juzu.impl.common.RunMode;
import juzu.test.AbstractWebTestCase;
import juzu.test.FileResource;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RunModeLiveAssetServletTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(RunMode.LIVE, true, "bridge.runmode.live.asset");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testAsset() throws Exception {
    String previousEtag = null;
    for (String expectedScript : new String[]{ "foo=0;", "foo=1;"}) {
      URL url = applicationURL();
      driver.get(url.toString());
      List<WebElement> scripts = driver.findElements(By.tagName("script"));
      String src  = scripts.get(0).getAttribute("src");
      HttpGet get = new HttpGet(new URL(url, src).toURI());
      HttpResponse response = HttpClientBuilder.create().build().execute(get);
      assertEquals(200, response.getStatusLine().getStatusCode());
      assertNotNull(response.getEntity());
      Header[] etagHeader = response.getHeaders("ETag");
      assertEquals(1, etagHeader.length);
      String etag = etagHeader[0].getValue();
      assertNotSame(previousEtag, etag);
      previousEtag = etag;
      Header[] cacheControlHeader = response.getHeaders("Cache-Control");
      assertEquals(1, cacheControlHeader.length);
      assertEquals("no-cache, no-store, must-revalidate", cacheControlHeader[0].getValue());
      assertEquals(expectedScript, EntityUtils.toString(response.getEntity()));
      FileResource<?> index = getCompiler().assertSource("bridge.runmode.live.asset.assets.test", "js");
      index.assertSave("foo=1;");
    }
  }
}
