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

import juzu.impl.request.EntityMarshaller;
import juzu.test.AbstractWebTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Julien Viet
 */
public abstract class AbstractResponseEntityWriter extends AbstractWebTestCase {

  public static WebArchive createDeployment(WebArchive war) {
    war.addAsServiceProvider(EntityMarshaller.class, FooWriter.class);
    return war;
  }

  @Drone
  WebDriver driver;

  @Test
  public void testPost() throws Exception {
    driver.get(applicationURL().toString());
    WebElement elt = driver.findElement(By.id("view"));
    String url = elt.getText();
    HttpGet get = new HttpGet(url);
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(get);
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertEquals("text/foo;charset=ISO-8859-1", response.getEntity().getContentType().getValue());
    assertNotNull(response.getEntity());
    assertEquals("from_view", EntityUtils.toString(response.getEntity()));
  }
}
