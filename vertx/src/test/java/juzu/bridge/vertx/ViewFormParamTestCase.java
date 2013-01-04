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

package juzu.bridge.vertx;

import com.jayway.restassured.http.ContentType;
import junit.framework.Assert;
import org.junit.Test;
import org.vertx.java.test.TestModule;

import static com.jayway.restassured.RestAssured.expect;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@TestModule(
    name = "juzu-v1.0",
    jsonConfig = "{ \"main\":\"viewformparam\"}")
public class ViewFormParamTestCase extends VertxTestCase {

  /** . */
  public static String view;

  @Test
  public void testFoo() throws Exception {
    expect().statusCode(200).given().contentType(ContentType.URLENC).body("param=foo").when().post("http://localhost:8080/view");
    Assert.assertEquals("foo", view);
  }
}
