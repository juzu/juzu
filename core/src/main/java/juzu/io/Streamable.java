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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface Streamable<S extends Stream> {

  public static class CharSequence implements Streamable<Stream.Char> {

    private final java.lang.CharSequence s;

    public CharSequence(java.lang.CharSequence s) {
      this.s = s;
    }

    public void send(Stream.Char stream) throws IOException {
      stream.append(s);
    }
  }

  public static class InputStream implements Streamable<Stream.Binary> {

    /** . */
    private final java.io.InputStream in;

    public InputStream(java.io.InputStream in) {
      this.in = in;
    }

    public void send(Stream.Binary stream) throws IOException {
      byte[] buffer = new byte[256];
      for (int l;(l = in.read(buffer)) != -1;) {
        stream.append(buffer, 0, l);
      }
    }
  }

  void send(S stream) throws IOException;

}
