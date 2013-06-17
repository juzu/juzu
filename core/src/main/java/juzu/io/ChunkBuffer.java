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

import java.io.IOException;
import java.util.LinkedList;

/** @author Julien Viet */
public class ChunkBuffer implements Streamable, Appendable {


  /** . */
  private static final int STATUS_BUFFERING = 0;

  /** . */
  private static final int STATUS_PROVIDING = 1;

  /** . */
  private static final int STATUS_CLOSED = 2;

  /** . */
  private final LinkedList<Chunk> queue = new LinkedList<Chunk>();

  /** . */
  private Stream consumer = null;

  /** . */
  private int status = STATUS_BUFFERING;

  /** . */
  private final Object lock = new Object();

  /** . */
  private Thread.UncaughtExceptionHandler errorHandler;

  public ChunkBuffer() {
  }

  public ChunkBuffer(Thread.UncaughtExceptionHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public Appendable append(CharSequence csq) throws IOException {
    return append(Chunk.create(csq));
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    return append(Chunk.create(csq));
  }

  public Appendable append(char c) throws IOException {
    return append(Chunk.create(c));
  }

  public ChunkBuffer append(Iterable<Chunk> chunks) {
    for (Chunk chunk : chunks) {
      append(chunk);
    }
    return this;
  }

  public ChunkBuffer append(Chunk chunk) {
    synchronized (lock) {
      switch (status) {
        case STATUS_CLOSED:
          throw new IllegalArgumentException("Already closed");
        case STATUS_BUFFERING:
          queue.add(chunk);
          break;
        case STATUS_PROVIDING:
          consumer.provide(chunk);
          break;
      }
    }
    return this;
  }

  public void send(Stream stream) {

    //
    synchronized (lock) {
      if (this.consumer != null) {
        throw new IllegalStateException("Already consumed");
      } else {
        this.consumer = stream;
      }
    }

    //
    boolean close;
    while (true) {
      Chunk chunk;
      synchronized (lock) {
        if (queue.isEmpty()) {
          if (status == STATUS_CLOSED) {
            close = true;
          } else {
            status = STATUS_PROVIDING;
            close = false;
          }
          break;
        } else {
          chunk = queue.removeFirst();
        }
      }
      stream.provide(chunk);
    }

    //
    if (close) {
      stream.close(errorHandler);
    }
  }

  public ChunkBuffer close() {
    Stream stream;
    synchronized (lock) {
      switch (status) {
        case STATUS_CLOSED:
          stream = null;
          break;
        case STATUS_PROVIDING:
          stream = this.consumer;
          status = STATUS_CLOSED;
          break;
        case STATUS_BUFFERING:
          stream = null;
          status = STATUS_CLOSED;
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    if (stream != null) {
      stream.close(errorHandler);
    }
    return this;
  }
}
