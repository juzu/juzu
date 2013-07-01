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

import juzu.io.UndeclaredIOException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.CharacterCodingException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PercentCodec extends BigInteger {

  /** . */
  public static final PercentCodec RFC3986_GEN_DELIMS;

  /** . */
  public static final PercentCodec RFC3986_SUB_DELIMS;

  /** . */
  public static final PercentCodec RFC3986_RESERVED_;

  /** . */
  public static final PercentCodec RFC3986_UNRESERVED;

  /** . */
  public static final PercentCodec RFC3986_PCHAR;

  /** . */
  public static final PercentCodec RFC3986_SEGMENT;

  /** . */
  public static final PercentCodec RFC3986_PATH;

  /** . */
  public static final PercentCodec RFC3986_QUERY;

  static {

    // rfc3986 : http://www.ietf.org/rfc/rfc3986.txt

    // gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
    RFC3986_GEN_DELIMS = PercentCodec.create(Tools.bitSet(":/?#[]&"));

    // sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
    RFC3986_SUB_DELIMS = PercentCodec.create(Tools.bitSet("!$&'()*+,;="));

    // reserved = gen-delims / sub-delims
    RFC3986_RESERVED_ = PercentCodec.create(RFC3986_GEN_DELIMS.or(RFC3986_SUB_DELIMS));

    // unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
    StringBuilder sb = new StringBuilder();
    for (char c = 'A';c <= 'Z';c++) {
      sb.append(c);
    }
    for (char c = 'a';c <= 'z';c++) {
      sb.append(c);
    }
    for (char c = '0';c <= '9';c++) {
      sb.append(c);
    }
    sb.append("_.-~");
    RFC3986_UNRESERVED = PercentCodec.create(Tools.bitSet(sb));

    // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
    RFC3986_PCHAR = PercentCodec.create(RFC3986_UNRESERVED.or(RFC3986_SUB_DELIMS).or(Tools.bitSet(":@")));

    // segment  = pchar
    RFC3986_SEGMENT = PercentCodec.create(RFC3986_PCHAR);

    // path = segment / "/"
    RFC3986_PATH = PercentCodec.create(RFC3986_SEGMENT.or(Tools.bitSet("/")));

    // query / "/" / "?"
    RFC3986_QUERY = PercentCodec.create(RFC3986_PCHAR.or(Tools.bitSet("/?")));
  }

  /** Not defined by the RFC. */
  public static final PercentCodec RFC3986_QUERY_PARAM_NAME;

  /** Not defined by the RFC. */
  public static final PercentCodec RFC3986_QUERY_PARAM_VALUE;

  static {
    RFC3986_QUERY_PARAM_NAME = PercentCodec.create(RFC3986_QUERY.clearBit('='));
    RFC3986_QUERY_PARAM_VALUE = PercentCodec.create(RFC3986_QUERY.clearBit('&'));
  }

  /** . */
  public static final char[] ALPHABET = "0123456789ABCDEF".toCharArray();

  public static PercentCodec create(BigInteger bitSet) {
    if (bitSet instanceof PercentCodec) {
      return (PercentCodec)bitSet;
    } else {
      return new PercentCodec(bitSet);
    }
  }

  private PercentCodec(BigInteger val) {
    super(val.toByteArray());
  }
  
  public boolean accept(char c) {
    return testBit(c);
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
    if (c < 2 << 6) {
      if (testBit(c)) {
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

  public String safeDecode(CharSequence s) throws UndeclaredIOException {
    try {
      return decode(s);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String decode(CharSequence s) throws IllegalArgumentException, UndeclaredIOException {
    try {
      StringBuilder sb = new StringBuilder(s.length());
      decode(s, sb);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public void decode(CharSequence s, Appendable appendable) throws IllegalArgumentException, IOException {
    decode(s, 0, s.length(), appendable);
  }

  public void decode(CharSequence s, int from, int len, Appendable to) throws IllegalArgumentException, IOException {
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
  public int decodeChar(CharSequence s, int from, int len, Appendable to) throws IllegalArgumentException, IOException {
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

  public static int hex(char c) {
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
