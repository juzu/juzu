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

import juzu.impl.common.Content;
import juzu.impl.common.Timestamped;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class RAMURLConnection extends URLConnection {

  /** . */
  private final Timestamped<Content> content;

  public RAMURLConnection(URL url, Timestamped<Content> content) {
    super(url);

    //
    this.content = content;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return content.getObject().getInputStream();
  }

  @Override
  public long getLastModified() {
    return content.getTime();
  }
}
