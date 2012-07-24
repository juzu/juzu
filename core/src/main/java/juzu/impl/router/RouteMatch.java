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

package juzu.impl.router;

import juzu.UndeclaredIOException;
import juzu.impl.common.MimeType;
import juzu.impl.common.QualifiedName;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMatch {

  /** The matched route. */
  final Route route;

  /** The matched parameters. */
  final Map<Param, String> matched;

  /** The un matched parameters. */
  final Map<QualifiedName, String> unmatched;

  RouteMatch(Route route, RenderContext context) {
    Map<Param, String> matched = Collections.emptyMap();
    Map<QualifiedName, String> unmatched = Collections.emptyMap();
    for (QualifiedName name : context.getNames()) {
      RenderContext.Parameter parameter = context.getParameter(name);
      if (parameter.isMatched()) {
        if (matched.isEmpty()) {
          matched = new HashMap<Param, String>();
        }
        matched.put(parameter.getParam(), parameter.getMatch());
      } else {
        if (unmatched.isEmpty()) {
          unmatched = new HashMap<QualifiedName, String>();
        }
        unmatched.put(parameter.getName(), parameter.getValue());
      }
    }

    //
    this.route = route;
    this.matched = Collections.unmodifiableMap(matched);
    this.unmatched = Collections.unmodifiableMap(unmatched);
  }

  RouteMatch(Route route, Map<Param, String> matched) {
    this.route = route;
    this.matched = Collections.unmodifiableMap(matched);
    this.unmatched = Collections.emptyMap();
  }

  public void render(URIWriter writer) throws IOException {

    // Append path first
    route.renderPath(this, writer, false);

    // Append query parameters after
    route.renderQueryString(this, writer);
  }

  public Route getRoute() {
    return route;
  }

  public Map<Param, String> getMatched() {
    return Collections.unmodifiableMap(matched);
  }

  public Map<QualifiedName, String> getUnmatched() {
    return unmatched;
  }

  public String render() {
    try {
      StringBuilder sb = new StringBuilder();
      URIWriter writer = new URIWriter(sb, MimeType.PLAIN);
      render(writer);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }
}
