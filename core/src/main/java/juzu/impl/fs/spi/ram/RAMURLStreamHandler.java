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

package juzu.impl.fs.spi.ram;

import juzu.impl.common.Resource;
import juzu.impl.common.Spliterator;
import juzu.impl.common.Timestamped;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMURLStreamHandler extends URLStreamHandler {

  /** . */
  private RAMFileSystem fs;

  public RAMURLStreamHandler(RAMFileSystem fs) {
    this.fs = fs;
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    Iterable<String> names = Spliterator.split(u.getPath().substring(1), '/');
    String[] path = fs.getPath(names);
    Timestamped<Resource> resource = null;
    if (path != null) {
      resource = fs.getResource(path);
    }
    if (resource != null) {
      return new RAMURLConnection(u, resource);
    }
    throw new IOException("Could not connect to non existing resource " + names);
  }
}
