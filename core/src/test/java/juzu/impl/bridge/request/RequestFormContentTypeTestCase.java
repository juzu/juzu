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

import juzu.impl.bridge.DescriptorBuilder;
import juzu.impl.common.Tools;
import juzu.test.AbstractWebTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/** @author <a href="mailto:benjamin.garcia@geomatys.com">Benjamin Garcia</a> */
public class RequestFormContentTypeTestCase extends AbstractWebTestCase {

  /** . */
  public static String param;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return createServletDeployment(DescriptorBuilder.DEFAULT.requestEncoding(Tools.ISO_8859_1).servletApp("bridge.request.formcontenttype.view"), true);
  }

  @Test
  public void testWithCharsetOnContentType() throws Exception {
    HttpClient client = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(applicationURL().toString() + "/foo");
    ContentType content = ContentType.create("application/x-www-form-urlencoded", "UTF-8");
    HttpEntity entity = new StringEntity("param=" + "test" + EURO, content);
    httpPost.setEntity(entity);
    client.execute(httpPost);
    assertEquals("test" + EURO, param);
  }

  @Test
  public void testWithoutCharsetOnContentType() throws Exception {
    HttpClient client = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(applicationURL().toString() + "/foo");
    ContentType content = ContentType.create("application/x-www-form-urlencoded");
    HttpEntity entity = new StringEntity("param=" + "test", content);
    httpPost.setEntity(entity);
    client.execute(httpPost);
    assertEquals("test", param);
  }
}
