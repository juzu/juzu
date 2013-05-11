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

import juzu.impl.common.UriBuilder;
import juzu.io.UndeclaredIOException;
import juzu.impl.common.MimeType;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMatch {

  /** The matched route. */
  final Route route;

  /** The matched parameters. */
  final Map<PathParam, String> matched;

  /** The un matched parameters. */
  final Map<String, String> unmatched;

  RouteMatch(Route route, Map<PathParam, String> matched) {
    this.route = route;
    this.matched = Collections.unmodifiableMap(matched);
    this.unmatched = Collections.emptyMap();
  }

  RouteMatch(Route route, Map<String, String> unmatched, Map<PathParam, String> matched) {
    this.route = route;
    this.matched = Collections.unmodifiableMap(matched);
    this.unmatched = Collections.unmodifiableMap(unmatched);
  }

  public Route getRoute() {
    return route;
  }

  public Map<PathParam, String> getMatched() {
    return Collections.unmodifiableMap(matched);
  }

  public Map<String, String> getUnmatched() {
    return unmatched;
  }

  public void render(UriBuilder writer) throws IOException {
    route.renderPath(this, writer, false);
  }

  public String render() {
    try {
      StringBuilder sb = new StringBuilder();
      UriBuilder writer = new UriBuilder(sb, MimeType.PLAIN);
      render(writer);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }
}
