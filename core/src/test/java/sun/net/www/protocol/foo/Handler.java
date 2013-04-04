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
