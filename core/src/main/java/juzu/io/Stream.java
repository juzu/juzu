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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface Stream extends Flushable, Closeable {
  /** A stream that extends the appendable interface and add support for the {@link juzu.io.CharArray} class. */
  interface Char extends Stream, Appendable {

    Char append(CharArray chars) throws IOException;

    Char append(CharSequence csq) throws IOException;

    Char append(CharSequence csq, int start, int end) throws IOException;

    Char append(char c) throws IOException;

  }

  /** A binary stream. */
  interface Binary extends Stream {

    Binary append(byte[] data) throws IOException;

    Binary append(byte[] data, int off, int len) throws IOException;

  }
}
