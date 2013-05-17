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

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static juzu.impl.common.Tools.UTF_8;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AppendableStreamTestCase extends AbstractTestCase {

  @Test
  public void testSimple() throws IOException {
    StringBuilder buffer = new StringBuilder();
    AppendableStream bos = new AppendableStream(UTF_8, buffer);
    bos.append("HELLO".getBytes(UTF_8));
    assertEquals("HELLO", buffer.toString());
  }

  @Test
  public void testGreaterThanBuffer() throws IOException {
    StringBuilder buffer = new StringBuilder();
    AppendableStream bos = new AppendableStream(UTF_8, buffer);
    ByteArrayOutputStream sb = new ByteArrayOutputStream();
    while (sb.size() <= BinaryStream.BUFFER_SIZE) {
      sb.write("A".getBytes(UTF_8));
    }
    bos.append(sb.toByteArray());
    assertEquals(sb.toString(UTF_8.name()), buffer.toString());
  }

  @Test
  public void testAllChars() throws IOException {
    StringBuilder buffer = new StringBuilder();
    AppendableStream bos = new AppendableStream(UTF_8, buffer);
    for (char c = Character.MIN_VALUE;c < Character.MAX_VALUE;c++) {
      try {
        bos.append(Character.toString(c));
        buffer.setLength(0);
      }
      catch (UnsupportedOperationException e) {
        throw e;
      }
    }
  }
}
