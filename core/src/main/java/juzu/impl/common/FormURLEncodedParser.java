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

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Basic implementation that needs improvements (make configurable and reuse PercentCodec to use any encoding)
 *
 * @author Julien Viet
 */
public class FormURLEncodedParser extends AbstractParameterParser {

  /** . */
  private final Charset encoding;

  public FormURLEncodedParser(Charset encoding, CharSequence s, int from, int to) {
    super(s, from, to);

    //
    this.encoding = encoding;
  }

  @Override
  protected String safeDecodeName(String s) {
    return safeDecodeValue(s);
  }

  @Override
  protected String safeDecodeValue(String s) {
    int len = s.length();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(20);
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < len) {
      char c = s.charAt(i);
      if (c == '%') {
        if (i + 2 < len) {
          int i1 = PercentCodec.hex(s.charAt(i + 1));
          int i2 = PercentCodec.hex(s.charAt(i + 2));
          int b = i1 * 0x10 + i2;
          baos.write(b);
          i += 3;
        } else {
          return null;
        }
      } else {
        if (baos.size() > 0) {
          sb.append(new String(baos.toByteArray(), encoding));
          baos.reset();
        }
        sb.append(c);
        i++;
      }
    }
    if (baos.size() > 0) {
      sb.append(new String(baos.toByteArray(), encoding));
      baos.reset();
    }
    return sb.toString();
  }
}
