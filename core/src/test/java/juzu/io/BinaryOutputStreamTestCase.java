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

package juzu.io;

import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static juzu.impl.common.Tools.UTF_8;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BinaryOutputStreamTestCase extends AbstractTestCase {

  @Test
  public void testFoo() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bos = OutputStream.create(UTF_8, buffer);
    bos.append("content[" + EURO + "]");
    assertEquals("content[" + EURO + "]", new String(buffer.toByteArray(), UTF_8));
  }

  @Test
  public void testSingle() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bos = OutputStream.create(UTF_8, buffer);
    bos.append('H');
    bos.append('E');
    bos.append('L');
    bos.append('L');
    bos.append('O');
    assertEquals("HELLO", new String(buffer.toByteArray(), UTF_8));
  }

  @Test
  public void testSimple() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bos = OutputStream.create(UTF_8, buffer);
    bos.append("HELLO");
    assertEquals("HELLO", new String(buffer.toByteArray(), UTF_8));
  }

  @Test
  public void testGreaterThanBuffer() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bos = OutputStream.create(UTF_8, buffer);
    StringBuilder sb = new StringBuilder();
    while (sb.length() <= OutputStream.BUFFER_SIZE) {
      sb.append('a');
    }
    bos.append(sb);
    assertEquals(sb.toString(), new String(buffer.toByteArray(), UTF_8));
  }

  @Test
  public void testAllChars() throws IOException {
    for (char c = Character.MIN_VALUE;c < Character.MAX_VALUE;c++) {
      try {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream bos = OutputStream.create(UTF_8, buffer);
        bos.append(Character.toString(c));
      }
      catch (UnsupportedOperationException e) {
        System.out.println("c = " + (int)c);
        throw e;
      }
    }
  }

  @Test
  public void testUnmappable() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bos = OutputStream.create(Tools.ISO_8859_1, buffer);
    bos.append("a" + EURO + "b");
    assertEquals("ab", buffer.toString());
  }
}
