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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Content {

  /** . */
  private byte[] data;

  /** . */
  private Charset encoding;

  public Content(byte[] data, Charset encoding) {
    if (data == null) {
      throw new NullPointerException("No null data accepted");
    }

    //
    this.data = data;
    this.encoding = encoding;
  }

  public Content(CharSequence s) {
    this(s, Charset.defaultCharset());
  }

  public Content(CharSequence s, Charset encoding) {
    this.encoding = encoding;
    this.data = s.toString().getBytes(encoding);
  }

  public Charset getEncoding() {
    return encoding;
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(data);
  }

  public CharSequence getCharSequence(Charset encoding) {
    return new String(data, encoding);
  }

  public CharSequence getCharSequence() {
    if (encoding == null) {
      throw new IllegalStateException("No encoding set");
    }
    return new String(data, encoding);
  }

  public int getSize() {
    return data.length;
  }
}
