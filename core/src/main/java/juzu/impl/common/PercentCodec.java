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

import juzu.UndeclaredIOException;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.BitSet;

/**
 * Utility class for performing percent encoding / decoding.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class PercentCodec {

  /** Path segment. */
  public static final PercentCodec PATH_SEGMENT;

  /** Path. */
  public static final PercentCodec PATH;

  /** Query params name or value. */
  public static final PercentCodec QUERY_PARAM;

  static {
    BitSet allowed = new BitSet(128);

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
    allowed.or(PATH_SEGMENT.allowed);
    allowed.set('/');
    PATH = new PercentCodec(allowed);
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

  /** . */
  private final BitSet allowed;

  private PercentCodec(BitSet allowed) {
    this.allowed = allowed;
  }

  public boolean accept(char c) {
    return c < 128 && allowed.get(c);
  }

  public void encodeSequence(CharSequence s, Appendable appendable) throws IOException {
    for (int len = s.length(), i = 0;i < len;i++) {
      char c = s.charAt(i);
      encodeChar(c, appendable);
    }
  }

  public String encodeSequence(CharSequence s) {
    try {
      StringBuilder sb = new StringBuilder(s.length());
      encodeSequence(s, sb);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public void encodeChar(char c, Appendable appendable) throws IOException {
    if (c < 2 << 6) {
      if (allowed.get(c)) {
        appendable.append(c);
      } else {
        appendable.append('%');
        appendable.append(ALPHABET[(c & 0xF0) >> 4]);
        appendable.append(ALPHABET[c & 0xF]);
      }
    } else if (c < 2 << 10) {
      int c0 = 0x80 | (c & 0x3F);
      int c1 = 0xC0 | ((c & 0x7C0) >> 6);
      appendable.append('%');
      appendable.append(ALPHABET[(c1 & 0xF0) >> 4]);
      appendable.append(ALPHABET[c1 & 0xF]);
      appendable.append('%');
      appendable.append(ALPHABET[(c0 & 0xF0) >> 4]);
      appendable.append(ALPHABET[c0 & 0xF]);
    } else if (c < 2 << 15) {
      int c0 = 0x80 | (c & 0x3F);
      int c1 = 0x80 | ((c & 0xFC0) >> 6);
      int c2 = 0xE0 | ((c & 0xF000) >> 12);
      appendable.append('%');
      appendable.append(ALPHABET[(c2 & 0xF0) >> 4]);
      appendable.append(ALPHABET[c2 & 0xF]);
      appendable.append('%');
      appendable.append(ALPHABET[(c1 & 0xF0) >> 4]);
      appendable.append(ALPHABET[c1 & 0xF]);
      appendable.append('%');
      appendable.append(ALPHABET[(c0 & 0xF0) >> 4]);
      appendable.append(ALPHABET[c0 & 0xF]);
    } else {
      // Java primitive type cannot handle more than 16 bits
      throw new CharacterCodingException();
    }
  }

  public String decodeSequence(CharSequence s) throws UndeclaredIOException {
    try {
      StringBuilder sb = new StringBuilder(s.length());
      decodeSequence(s, sb);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public void decodeSequence(CharSequence s, Appendable appendable) throws IOException {
    decodeSequence(s, 0, s.length(), appendable);
  }

  public void decodeSequence(CharSequence s, int from, int len, Appendable to) throws IOException {
    while (len > 0) {
      int delta = decodeChar(s, from, len, to);
      len -= delta;
      from += delta;
    }
  }

  /**
   * Decode a single char.
   *
   * @param s the sequence
   * @param from the offset
   * @param len the len of the sequence
   * @param to the destination
   * @return the number of consumed chars
   * @throws IOException
   */
  public int decodeChar(CharSequence s, int from, int len, Appendable to) throws IOException {
    final int prev = len;
    char c = s.charAt(from++);
    if (c == '%') {
      if (len < 3) {
        throw new IllegalArgumentException();
      } else {
        len -= 3;
        char c1 = (char)((hex(s.charAt(from++)) << 4) + hex(s.charAt(from++)));
        if ((c1 & 0x80) == 0x00) {
          to.append(c1);
        } else {
          if (len < 3) {
            throw new IllegalArgumentException();
          } else {
            if (s.charAt(from++) != '%') {
              throw new IllegalArgumentException();
            }
            len -= 3;
            char c2 = (char)((hex(s.charAt(from++)) << 4) + hex(s.charAt(from++)));
            if ((c1 & 0xE0) == 0xC0) {
              to.append((char)(((c1 & 0x1F) << 6) + (c2 & 0x3F)));
            } else {
              if (len < 3) {
                throw new IllegalArgumentException();
              } else {
                if (s.charAt(from++) != '%') {
                  throw new IllegalArgumentException();
                }
                len -= 3;
                char c3 = (char)((hex(s.charAt(from++)) << 4) + hex(s.charAt(from++)));
                if ((c1 & 0xF0) == 0xE0) {
                  to.append((char)(((c1 & 0x0F) << 12) + ((c2 & 0x3F) << 6) + (c3 & 0x3F)));
                } else {
                  // Java primitive type cannot handle more than 16 bits
                  throw new IllegalArgumentException();
                }
              }
            }
          }
        }
      }
    } else {
      if (accept(c)) {
        to.append(c);
        len--;
      } else {
        throw new IllegalArgumentException("Illegal char " + (int)c);
      }
    }
    return prev - len;
  }

  private int hex(char c) throws IOException {
    if (c >= '0' && c <= '9') {
      return c - '0';
    }
    else if (c >= 'A' && c <= 'F') {
      return c + 10 - 'A';
    }
    else if (c >= 'a' && c <= 'f') {
      return c + 10 - 'a';
    } else {
      throw new IllegalArgumentException();
    }
  }
}
