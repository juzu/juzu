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
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AppendableStream extends CharStream {

  /** . */
  private final Appendable out;

  /** . */
  private final Flushable flushable;

  /** . */
  private final Closeable closeable;

  public AppendableStream(Charset charset, Appendable out) {
    super(charset);

    //
    this.out = out;
    this.flushable = null;
    this.closeable = null;
  }

  public AppendableStream(Charset charset, Appendable out, Flushable flushable, Closeable closeable) {
    super(charset);

    //
    this.out = out;
    this.flushable = flushable;
    this.closeable = closeable;
  }

  @Override
  public Stream append(CharSequence csq) throws IOException {
    out.append(csq);
    return this;
  }

  @Override
  public Stream append(CharSequence csq, int start, int end) throws IOException {
    out.append(csq, start, end);
    return this;
  }

  @Override
  public Stream append(char c) throws IOException {
    out.append(c);
    return this;
  }

  public void close() throws IOException {
    if (closeable != null) {
      closeable.close();
    }
  }

  public void flush() throws IOException {
    if (flushable != null) {
      flushable.flush();
    }
  }
}
