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
package juzu.impl.bridge.request;

import juzu.impl.common.Tools;
import juzu.impl.request.EntityUnmarshaller;
import juzu.test.AbstractWebTestCase;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Julien Viet
 */
public abstract class AbstractRequestEntityReader extends AbstractWebTestCase {

  public static WebArchive createDeployment(WebArchive war) {
    war.addAsServiceProvider(EntityUnmarshaller.class, FooReader.class);
    return war;
  }

  public static byte[] data;

  @Drone
  WebDriver driver;

  @Test
  public void testPost() throws Exception {
    driver.get(applicationURL().toString());
    WebElement elt = driver.findElement(By.id("post"));
    String url = elt.getText();
    data = null;
    HttpPost post = new HttpPost(url);
    post.setEntity(new ByteArrayEntity("<foo></foo>".getBytes(Tools.ISO_8859_1), ContentType.create("text/foo", Tools.ISO_8859_1)));
    HttpClient client = HttpClientBuilder.create().build();
    client.execute(post);
    assertNotNull(data);
    assertEquals("<foo></foo>", new String(data, Tools.ISO_8859_1));
  }

}
