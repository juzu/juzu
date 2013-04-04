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

package juzu.impl.template.spi.juzu.ast;

import java.io.IOException;
import java.io.Reader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetReader extends Reader {

  /** . */
  private final Reader in;

  /** . */
  private final StringBuilder data;

  public OffsetReader(Reader in) {
    this.in = in;
    this.data = new StringBuilder();
  }

  public StringBuilder getData() {
    return data;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int read = in.read(cbuf, off, len);
    if (read > 0) {
      data.append(cbuf, off, read);
    }
    return read;
  }

  @Override
  public void close() throws IOException {
  }
}
