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

import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.MethodHandle;
import juzu.request.Result;
import juzu.impl.runtime.ApplicationRuntime;
import juzu.test.AbstractTestCase;
import org.junit.Assert;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge extends MockRequestBridge implements MimeBridge {

  public MockMimeBridge(ApplicationRuntime<?, ?> application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    super(application, client, target, parameters);
  }

  public String assertStringResult(String expected) {
    String actual = assertStringResult();
    Assert.assertEquals(expected, actual);
    return actual;
  }

  public String assertStringResult() {
    if (buffer == null) {
      throw AbstractTestCase.failure("No data");
    } else {
      try {
        return buffer.toString(charset.name());
      }
      catch (UnsupportedEncodingException e) {
        throw AbstractTestCase.failure(e);
      }
    }
  }

  public byte[] assertBinaryResult() {
    if (buffer == null) {
      throw AbstractTestCase.failure("No data");
    } else {
      return buffer.toByteArray();
    }
  }

  public String getMimeType() {
    return mimeType;
  }

  public void assertOk() {
    assertStatus(200);
  }

  public void assertNotFound() {
    assertStatus(404);
  }

  public void assertStatus(int status) {
    Result.Status content = AbstractTestCase.assertInstanceOf(Result.Status.class, result);
    Assert.assertEquals(status, content.code);
  }
}
