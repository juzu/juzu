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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CharStream implements Stream {

  /** Charset. */
  private Charset charset;

  /** Decoder. */
  private CharsetDecoder decoder;

  /** . */
  private CharBuffer bb;

  public CharStream(Charset charset) {
    this.charset = charset;
  }

  public abstract Stream append(CharSequence csq) throws IOException;

  public abstract Stream append(CharSequence csq, int start, int end) throws IOException;

  public abstract Stream append(char c) throws IOException;

  public Stream append(byte[] data) throws IOException {
    return append(data, 0, data.length);
  }

  public Stream append(byte[] data, int off, int len) throws IOException {
    return append(ByteBuffer.wrap(data, off, len));
  }

  public Stream append(ByteBuffer buffer) throws IOException {
    if (buffer.hasRemaining()) {
      if (decoder == null) {
        decoder = charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).onMalformedInput(CodingErrorAction.IGNORE);
        bb = CharBuffer.allocate(BUFFER_SIZE);
      } else {
        decoder.reset();
      }
      while (true) {
        CoderResult result ;
        result = buffer.hasRemaining() ? decoder.decode(buffer, bb, true) : decoder.flush(bb);
        if (result.isUnderflow() || result.isOverflow()) {
          bb.flip();
          if (bb.hasRemaining()) {
            append(bb);
          }
          bb.clear();
          if (result.isUnderflow()) {
            if (buffer.remaining() > 0) {
              throw new UnsupportedOperationException("We don't support this case yet");
            } else {
              break;
            }
          }
        } else {
          if (result.isUnmappable()) {
            buffer.position(buffer.position() + result.length());
          } else {
            throw new UnsupportedOperationException("We don't support this case yet (2) " + result);
          }
        }
      }
    }
    return this;
  }

  public Stream append(CharBuffer buffer) throws IOException {
    append(buffer, buffer.arrayOffset() + buffer.position(), buffer.limit() - buffer.arrayOffset());
    return this;
  }
}
