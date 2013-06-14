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

import java.util.Arrays;
import java.util.LinkedList;

/** @author Julien Viet */
public class ChunkBufferTestCase extends AbstractTestCase {

  static class Consumer extends LinkedList<Chunk> implements Stream {

    /** . */
    boolean closed = false;

    public void provide(Chunk chunk) {
      if (!closed) {
        add(chunk);
      }
    }

    public void close() {
      closed = true;
    }

    public void close(Thread.UncaughtExceptionHandler errorHandler) {
      closed = true;
    }
  }

  static class Simple extends Chunk {
  }

  /** . */
  static final Simple chunk1 = new Simple();

  /** . */
  static final Simple chunk2 = new Simple();

  @Test
  public void testConsumeClosed() {
    ChunkBuffer buffer = new ChunkBuffer();
    buffer.append(chunk1);
    buffer.append(chunk2);
    buffer.close();
    Consumer consumer = new Consumer();
    buffer.send(consumer);
    assertTrue(consumer.closed);
    assertEquals(Arrays.asList(chunk1, chunk2), consumer);
  }

  @Test
  public void testAsyncConsumer() {
    ChunkBuffer buffer = new ChunkBuffer();
    buffer.append(chunk1);
    Consumer consumer = new Consumer();
    buffer.send(consumer);
    assertFalse(consumer.closed);
    assertEquals(Arrays.asList(chunk1), consumer);
    buffer.append(chunk2);
    buffer.close();
    assertTrue(consumer.closed);
    assertEquals(Arrays.asList(chunk1, chunk2), consumer);
  }
}
