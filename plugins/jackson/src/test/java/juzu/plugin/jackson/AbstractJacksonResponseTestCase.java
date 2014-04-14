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

package juzu.plugin.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import juzu.test.AbstractWebTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractJacksonResponseTestCase extends AbstractWebTestCase {

  @Test
  public void testResponse() throws Exception {
    HttpGet get = new HttpGet(applicationURL().toString());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(get);
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(response.getEntity());
    assertEquals("application/json;charset=ISO-8859-1", response.getEntity().getContentType().getValue());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree = mapper.readTree(response.getEntity().getContent());
    JsonNodeFactory factory = JsonNodeFactory.instance;
    JsonNode expected = factory.objectNode().set("foo", factory.textNode("bar"));
    assertEquals(expected, tree);
  }
}
