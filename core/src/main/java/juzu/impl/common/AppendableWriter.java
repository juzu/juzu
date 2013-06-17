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

import java.io.IOException;
import java.io.Writer;

/** @author Julien Viet */
public class AppendableWriter extends Writer {

  /** . */
  private final Appendable appendable;

  public AppendableWriter(Appendable appendable) {
    this.appendable = appendable;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(cbuf, off, len);
    appendable.append(buffer);
  }

  @Override
  public Writer append(CharSequence csq) throws IOException {
    appendable.append(csq);
    return this;
  }

  @Override
  public Writer append(CharSequence csq, int start, int end) throws IOException {
    appendable.append(csq, start, end);
    return this;
  }

  @Override
  public Writer append(char c) throws IOException {
    appendable.append(c);
    return this;
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }
}
