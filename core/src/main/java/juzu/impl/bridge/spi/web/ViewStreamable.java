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
package juzu.impl.bridge.spi.web;

import juzu.impl.common.Tools;
import juzu.io.Stream;
import juzu.io.Streamable;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ViewStreamable implements Streamable {

  /** . */
  private final Streamable wrapped;

  /** . */
  private final boolean decorated;

  ViewStreamable(Streamable wrapped, boolean decorated) {
    this.wrapped = wrapped;
    this.decorated = decorated;
  }

  public void send(final Stream stream) throws IOException {

    Stream our = new Stream() {
      public Stream append(java.lang.CharSequence csq) throws IOException {
        stream.append(csq);
        return this;
      }

      public Stream append(java.lang.CharSequence csq, int start, int end) throws IOException {
        stream.append(csq, start, end);
        return this;
      }

      public Stream append(char c) throws IOException {
        stream.append(c);
        return this;
      }

      public Stream append(byte[] data) throws IOException {
        stream.append(data);
        return this;
      }

      public Stream append(byte[] data, int off, int len) throws IOException {
        stream.append(data, off, len);
        return this;
      }

      public void flush() throws IOException {
        stream.flush();
      }

      public void close() throws IOException {
        try {
          if (decorated) {
            sendFooter(stream);
          }
        }
        finally {
          Tools.safeClose(stream);
        }
      }
    };

    //
    wrapped.send(our);
  }

  private void sendFooter(Stream writer) throws IOException {
    writer.append("</body>\n");
    writer.append("</html>\n");
  }
}
