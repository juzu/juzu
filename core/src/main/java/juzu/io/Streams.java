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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;


/**
 * Implementation of the {@link juzu.io.Stream} interface that uses an appendable delegate.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Streams {

  public static Stream empty() {
    return new Stream() {
      public Stream append(ByteBuffer buffer) throws IOException {
        return this;
      }
      public Stream append(CharBuffer buffer) throws IOException {
        return this;
      }
      public Stream append(CharSequence csq) throws IOException {
        return this;
      }
      public Stream append(CharSequence csq, int start, int end) throws IOException {
        return this;
      }
      public Stream append(char c) throws IOException {
        return this;
      }
      public Stream append(byte[] data) throws IOException {
        return this;
      }
      public Stream append(byte[] data, int off, int len) throws IOException {
        return this;
      }
      public void close() throws IOException {
      }
      public void flush() throws IOException {
      }
    };
  }

  public static Stream appendable(Charset charset, Appendable appendable) {
    return new AppendableStream(charset, appendable);
  }

  public static <A extends java.lang.Appendable & Flushable> Stream flushable(Charset charset, final A appendable) {
    return new AppendableStream(charset, appendable, appendable, null);
  }

  public static <A extends java.lang.Appendable & Flushable & Closeable> Stream closeable(Charset charset, final A appendable) {
    return new AppendableStream(charset, appendable, appendable, appendable);
  }

  public static <A extends OutputStream & Flushable & Closeable> Stream closeable(Charset charset, final A appendable) {
    return new BinaryOutputStream(charset, appendable);
  }
}
