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

import juzu.impl.common.CharBuffer;
import juzu.impl.common.Tools;

import java.io.IOException;

/**
 * Basic implementation of an asynchronous streamable. This implementation is basic for now:
 *
 * <ul>
 *   <li>Use synchronized blocks.</li>
 *   <li>Does not do any kind of control flow.</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AsyncStreamable implements Streamable<Stream.Char>, Appendable {

  /** . */
  private final CharBuffer buffer = new CharBuffer(512);

  /** . */
  private boolean closed = false;

  /** . */
  private Stream.Char stream = null;

  public void send(Stream.Char stream) throws IOException {
    synchronized (buffer) {
      if (this.stream != null) {
        throw new IllegalStateException("Already streaming");
      } else {
        this.stream = stream;
        this.buffer.writeTo(stream);
        if (closed) {
          Tools.safeClose(stream);
        }
      }
    }
  }

  private Appendable delegate() {
    Stream.Char s = stream;
    if (s != null) {
      return s;
    } else {
      return buffer;
    }
  }

  public AsyncStreamable append(java.lang.CharSequence csq) throws IOException {
    synchronized (buffer) {
      delegate().append(csq);
    }
    return this;
  }

  public AsyncStreamable append(java.lang.CharSequence csq, int start, int end) throws IOException {
    synchronized (buffer) {
      delegate().append(csq, start, end);
    }
    return this;
  }

  public AsyncStreamable append(char c) throws IOException {
    synchronized (buffer) {
      delegate().append(c);
    }
    return this;
  }

  public void close() {
    synchronized (buffer) {
      if (!closed) {
        if (stream != null) {
          Tools.safeClose(stream);
        }
      }
    }
  }
}
