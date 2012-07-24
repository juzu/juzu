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

package sun.net.www.protocol.foo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Foo protocol for testing purposes.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Handler extends URLStreamHandler {

  /** . */
  private static final Map<String, byte[]> state = new HashMap<String, byte[]>();

  public static void bind(String key, byte[] bytes) {
    state.put(key, bytes);
  }

  public static void clear() {
    state.clear();
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    final byte[] bytes = state.get(u.getFile());
    return new URLConnection(u) {
      @Override
      public void connect() throws IOException {
        if (bytes == null) {
          throw new IOException("No content");
        }
      }

      @Override
      public InputStream getInputStream() throws IOException {
        if (bytes == null) {
          throw new IOException("No content");
        }
        return new ByteArrayInputStream(bytes);
      }
    };
  }
}
