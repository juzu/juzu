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

import juzu.io.OutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/** @author Julien Viet */
public abstract class CharStream extends OutputStream {

  /** Charset. */
  private final Charset charset;

  /** Decoder. */
  private CharsetDecoder decoder;

  /** . */
  private CharBuffer bb;

  protected CharStream(Charset charset) {
    this.charset = charset;
  }

  public void append(byte[] data) throws IOException {
    append(data, 0, data.length);
  }

  public void append(byte[] data, int off, int len) throws IOException {
    append(ByteBuffer.wrap(data, off, len));
  }

  public void append(ByteBuffer buffer) throws IOException {
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
  }

  public void append(CharBuffer buffer) throws IOException {
    append(buffer, buffer.arrayOffset() + buffer.position(), buffer.limit() - buffer.arrayOffset());
  }
}
