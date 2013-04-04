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
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * An array of chars.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface CharArray {

  /**
   * Returns the number of characters contained in this array.
   *
   * @return the length
   */
  int getLength();

  void write(OutputStream out) throws IOException, NullPointerException;

  void write(Appendable appendable) throws IOException, NullPointerException;

  public static class Simple implements CharArray {

    /** . */
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /** . */
    private final CharSequence chars;

    /** . */
    private final byte[] bytes;

    public Simple(CharSequence sequence) {
      try {
        this.chars = sequence;
        this.bytes = UTF_8.newEncoder().encode(CharBuffer.wrap(chars)).array();
      }
      catch (CharacterCodingException e) {
        throw new UndeclaredThrowableException(e);
      }
    }

    public int getLength() {
      return chars.length();
    }

    public void write(OutputStream out) throws IOException, NullPointerException {
      if (out == null) {
        throw new NullPointerException("No null dst argument accepted");
      }
      out.write(bytes);
    }

    public void write(Appendable appendable) throws IOException {
      if (appendable == null) {
        throw new NullPointerException("No null dst argument accepted");
      }
      appendable.append(chars);
    }
  }
}
