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
package juzu.impl.io;

import juzu.impl.common.Tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;

/** @author Julien Viet */
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
  public void append(CharSequence csq) throws IOException {
    out.append(csq);
  }

  @Override
  public void append(CharSequence csq, int start, int end) throws IOException {
    out.append(csq, start, end);
  }

  @Override
  public void append(char c) throws IOException {
    out.append(c);
  }

  public void close() {
    Tools.safeClose(closeable);
  }

  public void flush() throws IOException {
    Tools.safeFlush(flushable);
  }
}
