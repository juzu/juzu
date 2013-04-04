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


/**
 * Implementation of the {@link juzu.io.Stream.Char} interface that uses an appendable delegate.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AppendableStream implements Stream.Char {

  /** . */
  protected final Appendable delegate;

  public AppendableStream(Appendable delegate) {
    if (delegate == null) {
      throw new NullPointerException("No null writer accepted");
    }

    //
    this.delegate = delegate;
  }

  public Stream.Char append(char c) throws IOException {
    delegate.append(c);
    return this;
  }

  public Stream.Char append(CharSequence s) throws IOException {
    delegate.append(s);
    return this;
  }

  public Stream.Char append(CharSequence csq, int start, int end) throws IOException {
    delegate.append(csq, start, end);
    return this;
  }

  public Stream.Char append(CharArray chars) throws IOException {
    chars.write(delegate);
    return this;
  }

  public void close() throws IOException {
    if (delegate instanceof Closeable) {
      ((Closeable)delegate).close();
    }
  }

  public void flush() throws IOException {
    if (delegate instanceof Flushable) {
      ((Flushable)delegate).flush();
    }
  }
}
