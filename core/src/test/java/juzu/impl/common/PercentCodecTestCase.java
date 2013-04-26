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
  private static final PercentCodec codec = PercentCodec.QUERY_PARAM;

  @Test
  public void testEncode() {
    assertEquals("_", codec.encodeSequence("_"));
    assertEquals("a", codec.encodeSequence("a"));
    assertEquals("A", codec.encodeSequence("A"));
    assertEquals("0", codec.encodeSequence("0"));
    assertEquals("%25", codec.encodeSequence("%"));
    assertEquals("%5B", codec.encodeSequence("["));
    assertEquals("%7F", codec.encodeSequence(Character.toString(((char)127))));
    assertEquals("%C3%A7", codec.encodeSequence("ç"));
    assertEquals("%E2%82%AC", codec.encodeSequence("€"));
  }

  @Test
  public void testDecode() {
    assertEquals("_", codec.decodeSequence("_"));
    assertEquals("a", codec.decodeSequence("a"));
    assertEquals("A", codec.decodeSequence("A"));
    assertEquals("0", codec.decodeSequence("0"));
    assertEquals("%", codec.decodeSequence("%25"));
    assertEquals("[", codec.decodeSequence("%5B"));
    assertEquals(Character.toString(((char)127)), codec.decodeSequence("%7F"));
    assertEquals("ç", codec.decodeSequence("%C3%A7"));
    assertEquals("€", codec.decodeSequence("%E2%82%AC"));

    //
    assertDecodeException("%5_");
    assertDecodeException("%E2%82");

  }

  private void assertDecodeException(String coded) {
    try {
      codec.decodeSequence(coded);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

}
