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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Content {

  /** . */
  private long lastModified;

  /** . */
  private byte[] data;

  /** . */
  private Charset encoding;

  public Content(long lastModified, byte[] data, Charset encoding) {
    if (data == null) {
      throw new NullPointerException("No null data accepted");
    }

    //
    this.lastModified = lastModified;
    this.data = data;
    this.encoding = encoding;
  }

  public Content(long lastModified, CharSequence s) {
    this(lastModified, s, Charset.defaultCharset());
  }

  public Content(long lastModified, CharSequence s, Charset encoding) {
    this.encoding = encoding;
    this.lastModified = lastModified;
    this.data = s.toString().getBytes(encoding);
  }

  public long getLastModified() {
    return lastModified;
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

  public Content touch() {
    lastModified = System.currentTimeMillis();
    return this;
  }

  public int getSize() {
    return data.length;
  }
}
