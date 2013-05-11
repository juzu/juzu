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

package juzu.impl.router;

import juzu.impl.common.MimeType;
import juzu.impl.common.UriBuilder;

import javax.servlet.http.HttpUtils;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class URIHelper implements Appendable {

  /** . */
  private StringBuilder sb;

  /** . */
  final UriBuilder writer;

  public URIHelper() {
    this(new StringBuilder());
  }

  public URIHelper(StringBuilder sb) {
    this.sb = sb;
    this.writer = new UriBuilder(this, MimeType.PLAIN);
  }

  public String getPath() {
    if (sb != null) {
      int index = sb.indexOf("?");
      if (index != -1) {
        return sb.substring(0, index);
      }
      else {
        return sb.toString();
      }
    }
    return null;
  }

  public Map<String, String[]> getQueryParams() {
    if (sb != null) {
      int index = sb.indexOf("?");
      if (index != -1) {
        String query = sb.substring(index + 1);
        return HttpUtils.parseQueryString(query);
      }
    }
    return null;
  }

  public void reset() {
    if (sb != null) {
      sb.setLength(0);
    }
    writer.reset(sb);
  }

  public Appendable append(CharSequence csq) throws IOException {
    sb.append(csq);
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    sb.append(csq, start, end);
    return this;
  }

  public Appendable append(char c) throws IOException {
    sb.append(c);
    return this;
  }
}
