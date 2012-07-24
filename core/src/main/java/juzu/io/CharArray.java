/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
