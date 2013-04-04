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

package juzu.test.protocol.mock;

import juzu.Response;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.test.AbstractTestCase;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge extends MockRequestBridge implements MimeBridge {

  public MockMimeBridge(ApplicationLifeCycle<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    super(application, client, target, parameters);
  }

  public String assertStringResult(String expected) {
    String actual = assertStringResult();
    Assert.assertEquals(expected, actual);
    return actual;
  }

  public String assertStringResult() {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    AbstractTestCase.assertEquals(Stream.Char.class, content.getKind());
    try {
      StringBuilder builder = new StringBuilder();
      ((Response.Content<Stream.Char>)content).send(new AppendableStream(builder));
      return builder.toString();
    }
    catch (IOException e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public byte[] assertBinaryResult() {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    AbstractTestCase.assertEquals(Stream.Binary.class, content.getKind());
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BinaryOutputStream bos = new BinaryOutputStream(baos);
      ((Response.Content<Stream.Binary>)content).send(bos);
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public String getMimeType() {
    if (response instanceof Response.Content<?>) {
      return ((Response.Content)response).getMimeType();
    } else {
      return null;
    }
  }

  public void assertOk() {
    assertStatus(200);
  }

  public void assertNotFound() {
    assertStatus(404);
  }

  public void assertStatus(int status) {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    Assert.assertNotNull(content.getStatus());
    Assert.assertEquals((Integer)status, content.getStatus());
  }
}
