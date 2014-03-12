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

import juzu.test.AbstractWebTestCase;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ETagTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(true, "plugin.asset.etag");
  }

  @Drone
  WebDriver driver;

  @Test
  public void testSatisfied() throws Exception {

    driver.get(applicationURL().toString());
    WebElement element = driver.findElement(By.tagName("script"));
    String assetURI = element.getAttribute("src");
    assertNotNull(assetURI);
    HttpClient client = HttpClientBuilder.create().build();

    // With no etag
    HttpGet get = new HttpGet(assetURI);
    HttpResponse response = client.execute(get);
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertEquals("a = 0;", EntityUtils.toString(response.getEntity()));
    Header[] etag = response.getHeaders("ETag");
    assertNotNull(etag);
    assertEquals(1, etag.length);

    // With correct etag
    get = new HttpGet(assetURI);
    get.setHeader("If-None-Match", etag[0].getValue());
    response = client.execute(get);
    assertEquals(304, response.getStatusLine().getStatusCode());
    assertEquals(null, response.getEntity());

    // With wrong etag
    get = new HttpGet(assetURI);
    get.setHeader("If-None-Match", "foo");
    response = client.execute(get);
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertEquals("a = 0;", EntityUtils.toString(response.getEntity()));
    etag = response.getHeaders("ETag");
    assertNotNull(etag);
  }
}
