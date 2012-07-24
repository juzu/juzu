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

package juzu.impl.common;

import juzu.UndeclaredIOException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.BitSet;

/**
 * Utility class for performing percent encoding / decoding.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class PercentCodec {

  /** . */
  private static final byte[] codes;

  /** . */
  private static final int[] indices;

  static {

    Charset UTF8 = Charset.forName("UTF-8");
    CharsetEncoder encoder = UTF8.newEncoder();

    CharBuffer in = CharBuffer.allocate(1);
    ByteBuffer out = ByteBuffer.allocate(8);

    int size = 1000;

    // Original estimate size * 2
    byte[] _codes = new byte[size * 2];
    int[] _indices = new int[size + 1];

    int ptr = 0;

    for (char c = 0;c < size;c++) {
      switch (Character.getType(c)) {
        case Character.SURROGATE:
        case Character.PRIVATE_USE:
          break;
        default:
          if (encoder.canEncode(c)) {
            in.rewind();
            out.rewind();
            in.put(0, c);
            encoder.reset();
            encoder.encode(in, out, true);
            encoder.flush(out);
            int length = out.position();
            System.arraycopy(out.array(), 0, _codes, ptr, length);
            ptr += length;
          }
          else {
            //
          }
      }
      _indices[c + 1] = ptr;
    }

    //
    codes = _codes;
    indices = _indices;
  }

  static {
    BitSet allowed = new BitSet();

    // Unreserved
    for (char c = 'A';c <= 'Z';c++) {
      allowed.set(c);
    }
    for (char c = 'a';c <= 'z';c++) {
      allowed.set(c);
    }
    for (char c = '0';c <= '9';c++) {
      allowed.set(c);
    }
    allowed.set('_');
    allowed.set('.');
    allowed.set('-');
    allowed.set('~');

    // sub-delims
    allowed.set('!');
    allowed.set('$');
    allowed.set('&');
    allowed.set('\'');
    allowed.set('(');
    allowed.set(')');
    allowed.set('*');
    allowed.set('+');
    allowed.set(',');
    allowed.set(';');
    allowed.set('=');

    // ':' | '@'
    allowed.set(':');
    allowed.set('@');

    //
    PATH_SEGMENT = new PercentCodec(allowed);
  }

  static {
    BitSet allowed = new BitSet(128);
    for (char c = 'A';c <= 'Z';c++) {
      allowed.set(c);
    }
    for (char c = 'a';c <= 'z';c++) {
      allowed.set(c);
    }
    for (char c = '0';c <= '9';c++) {
      allowed.set(c);
    }
    allowed.set('_');
    allowed.set('.');
    allowed.set('-');
    allowed.set('~');

    // sub-delims without ( '&' | '=' )
    allowed.set('!');
    allowed.set('$');
    allowed.set('\'');
    allowed.set('(');
    allowed.set(')');
    allowed.set('*');
    allowed.set('+');
    allowed.set(',');
    allowed.set(';');

    // ':' | '@'
    allowed.set(':');
    allowed.set('@');

    // '?' | '/'
    allowed.set('?');
    allowed.set('/');

    //
    QUERY_PARAM = new PercentCodec(allowed);
  }

  /** . */
  private static final char[] ALPHABET = "0123456789ABCDEF".toCharArray();

  /** Path segment. */
  public static final PercentCodec PATH_SEGMENT;

  /** Query params name or value. */
  public static final PercentCodec QUERY_PARAM;

  /** . */
  private final BitSet allowed;

  private PercentCodec(BitSet allowed) {
    this.allowed = allowed;
  }

  public boolean accept(char c) {
    return c < allowed.length() && allowed.get(c);
  }

  public void encode(CharSequence s, Appendable appendable) throws IOException {
    for (int len = s.length(), i = 0;i < len;i++) {
      char c = s.charAt(i);
      encode(c, appendable);
    }
  }

  public String encode(CharSequence s) {
    try {
      StringBuilder sb = new StringBuilder(s.length());
      encode(s, sb);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public void encode(char c, Appendable appendable) throws IOException {
    if (accept(c)) {
      appendable.append(c);
    }
    else {
      if (c < indices.length - 1) {
        int from = indices[c];
        int to = indices[c + 1];
        while (from < to) {
          byte b = codes[from++];
          appendable.append('%');
          appendable.append(ALPHABET[(b & 0xF0) >> 4]);
          appendable.append(ALPHABET[b & 0xF]);
        }
      }
      else {
        throw new UnsupportedOperationException("Not handled yet");
      }
    }
  }
}
