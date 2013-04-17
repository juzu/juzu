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

import junit.framework.Assert;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CharBufferTestCase extends AbstractTestCase {

  @Test
  public void testSimple() {
    CharBuffer buffer = new CharBuffer();
    assertEquals(0, buffer.getLength());
    buffer.readFrom("a".toCharArray(), 0, 1);
    assertContent(buffer, "a");
  }

  @Test
  public void testResize() {
    CharBuffer buffer = new CharBuffer(4);
    assertEquals(4, buffer.getSize());
    buffer.readFrom("abcde".toCharArray(), 0, 5);
    assertTrue(buffer.getLength() > 4);
    assertContent(buffer, "abcde");
  }

  @Test
  public void testAppend() {
    CharBuffer buffer = new CharBuffer();
    buffer.readFrom("ab".toCharArray(), 0, 2);
    buffer.readFrom("cd".toCharArray(), 0, 2);
    assertContent(buffer, "abcd");
  }

  @Test
  public void testPartialRead() {
    CharBuffer buffer = new CharBuffer();
    buffer.readFrom("abcd".toCharArray(), 0, 4);
    Assert.assertEquals(4, buffer.getLength());
    assertContent(buffer, "ab");
    Assert.assertEquals(2, buffer.getLength());
    assertContent(buffer, "cd");
    Assert.assertEquals(0, buffer.getLength());
  }

  @Test
  public void testReadTooMuch() {
    CharBuffer buffer = new CharBuffer();
    buffer.readFrom("ab".toCharArray(), 0, 2);
    Assert.assertEquals(2, buffer.getLength());
    char[] tmp = "___".toCharArray();
    assertEquals(2, buffer.writeTo(tmp, 0, 3));
    Assert.assertEquals(0, buffer.getLength());
    Assert.assertEquals("ab_", new String(tmp));
  }

  @Test
  public void testReadAt() {
    CharBuffer buffer = new CharBuffer();
    buffer.readFrom("ab".toCharArray(), 0, 2);
    char[] tmp = "___".toCharArray();
    assertEquals(2, buffer.writeTo(tmp, 1, 2));
    Assert.assertEquals("_ab", new String(tmp));
  }

  @Test
  public void testNPE() {
    CharBuffer buffer = new CharBuffer();
    try {
      buffer.writeTo(null, 0, 1);
      fail();
    }
    catch (NullPointerException expected) {
    }
    try {
      buffer.readFrom(null, 0, 1);
      fail();
    }
    catch (NullPointerException expected) {
    }
  }

  @Test
  public void testIOOBE() {
    char[] tmp = new char[2];
    CharBuffer buffer = new CharBuffer();
    try {
      buffer.readFrom(tmp, 0, 3);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
    try {
      buffer.readFrom(tmp, -1, 2);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
    try {
      buffer.writeTo(tmp, -1, 2);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
    try {
      buffer.writeTo(tmp, 0, 3);
      fail();
    }
    catch (IndexOutOfBoundsException expected) {
    }
  }

  @Test
  public void testIAE() {
    char[] tmp = new char[2];
    CharBuffer buffer = new CharBuffer();
    try {
      buffer.readFrom(tmp, 0, -1);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      buffer.writeTo(tmp, 0, -1);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testAsAppendable() throws IOException {
    CharBuffer buffer = new CharBuffer();
    buffer.append("abc");
    assertContent(buffer, "abc");
  }

  @Test
  public void testWriteToAppendable() throws IOException {
    CharBuffer buffer = new CharBuffer();
    buffer.append("abc");
    StringBuilder sb = new StringBuilder();
    buffer.writeTo(sb);
    assertEquals("abc", sb.toString());
    assertEquals(0, buffer.getLength());
  }

  private void assertContent(CharBuffer buffer, String expected) {
    char[] tmp = new char[expected.length()];
    Assert.assertEquals(expected.length(), buffer.writeTo(tmp, 0, expected.length()));
    Assert.assertEquals(expected, new String(tmp));
  }
}
