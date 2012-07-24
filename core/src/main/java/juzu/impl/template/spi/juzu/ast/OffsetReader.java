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

package juzu.impl.template.spi.juzu.ast;

import java.io.IOException;
import java.io.Reader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetReader extends Reader {

  /** . */
  private final Reader in;

  /** . */
  private final StringBuilder data;

  public OffsetReader(Reader in) {
    this.in = in;
    this.data = new StringBuilder();
  }

  public StringBuilder getData() {
    return data;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int read = in.read(cbuf, off, len);
    if (read > 0) {
      data.append(cbuf, off, read);
    }
    return read;
  }

  @Override
  public void close() throws IOException {
  }
}
