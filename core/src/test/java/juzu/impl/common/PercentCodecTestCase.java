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
package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PercentCodecTestCase extends AbstractTestCase {

  /** . */
  private static final PercentCodec codec = PercentCodec.RFC3986_QUERY_PARAM_NAME;

  @Test
  public void testEncode() {
    assertEquals("_", codec.encode("_"));
    assertEquals("a", codec.encode("a"));
    assertEquals("A", codec.encode("A"));
    assertEquals("0", codec.encode("0"));
    assertEquals("%25", codec.encode("%"));
    assertEquals("%5B", codec.encode("["));
    assertEquals("%7F", codec.encode(Character.toString(((char)127))));
    assertEquals("%C3%A7", codec.encode("ç"));
    assertEquals("%E2%82%AC", codec.encode("€"));
  }

  @Test
  public void testDecode() {
    assertEquals("_", codec.decode("_"));
    assertEquals("a", codec.decode("a"));
    assertEquals("A", codec.decode("A"));
    assertEquals("0", codec.decode("0"));
    assertEquals("%", codec.decode("%25"));
    assertEquals("[", codec.decode("%5B"));
    assertEquals(Character.toString(((char)127)), codec.decode("%7F"));
    assertEquals("ç", codec.decode("%C3%A7"));
    assertEquals("€", codec.decode("%E2%82%AC"));

    //
    assertDecodeException("%5_");
    assertDecodeException("%E2%82");

  }

  private void assertDecodeException(String coded) {
    try {
      codec.decode(coded);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

}
