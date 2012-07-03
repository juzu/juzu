/*
 * Copyright (C) 2010 eXo Platform SAS.
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
import juzu.impl.router.metadata.ControllerDescriptor;
import juzu.impl.router.metadata.RouteDescriptor;
import juzu.impl.utils.MimeType;
import juzu.impl.utils.Tools;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * The router takes care of mapping a request to a a map.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Router {

  /** . */
  private static final BitSet escapeSet;

  static {
    // A subset of the path literals
    BitSet bs = new BitSet();
    bs.set('_');
    bs.set('.');
    bs.set('-');
    bs.set('~');
    bs.set('!');
    bs.set('$');
    bs.set('&');
    bs.set('+');
    bs.set(':');
    bs.set('@');

    //
    escapeSet = bs;
  }

  /** . */
  private final RegexFactory regexFactory;

  /** The root route. */
  final Route root;

  /** The slash escape char. */
  final char separatorEscape;

  /** . */
  final char separatorEscapeNible1;

  /** . */
  final char separatorEscapeNible2;

  /** . */
  private Regex[] regexes;

  public Router(ControllerDescriptor metaData) throws RouterConfigException {
    this(metaData, RegexFactory.JAVA);
  }

  public Router(ControllerDescriptor metaData, RegexFactory regexFactory) throws RouterConfigException {
    char separtorEscape = metaData.getSeparatorEscape();

    //
    int i = separtorEscape & ~0x7F;
    if (i > 0 || !escapeSet.get(separtorEscape)) {
      throw new RouterConfigException("Char " + (int)separtorEscape + " cannot be used a separator escape");
    }

    //
    String s = Integer.toString(separtorEscape, 16).toUpperCase();
    separatorEscapeNible1 = s.charAt(0);
    separatorEscapeNible2 = s.charAt(1);

    //
    this.regexFactory = regexFactory;
    this.root = new Route(this);
    this.separatorEscape = separtorEscape;
    this.regexes = new Regex[0];

    //
    for (RouteDescriptor routeMetaData : metaData.getRoutes()) {
      root.append(routeMetaData);
    }
  }

  Regex compile(String pattern) {
    for (Regex regex : regexes) {
      if (regex.getPattern().equals(pattern)) {
        return regex;
      }
    }
    Regex regex = regexFactory.compile(pattern);
    regex.index = regexes.length;
    regexes = Tools.appendTo(regexes, regex);
    return regex;
  }

  public void render(Map<QualifiedName, String> parameters, URIWriter writer) throws IOException {
    render(new RenderContext(parameters), writer);
  }

  public String render(Map<QualifiedName, String> parameters) {
    return render(new RenderContext(parameters));
  }

  public void render(RenderContext context, URIWriter writer) throws IOException {
    if (context.matchers == null) {
      context.matchers = new Regex.Matcher[regexes.length];
    }
    root.render(context, writer);
  }

  public String render(RenderContext context) {
    try {
      StringBuilder sb = new StringBuilder();
      URIWriter renderContext = new URIWriter(sb, MimeType.PLAIN);
      render(context, renderContext);
      return sb.toString();
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public Map<QualifiedName, String> route(String path) throws IOException {
    return route(path, Collections.<String, String[]>emptyMap());
  }

  public Map<QualifiedName, String> route(String path, Map<String, String[]> queryParams) {
    Iterator<Map<QualifiedName, String>> matcher = matcher(path, queryParams);
    if (matcher.hasNext()) {
      return matcher.next();
    }
    else {
      return null;
    }
  }

  public Iterator<Map<QualifiedName, String>> matcher(String path, Map<String, String[]> queryParams) {
    return root.route(path, queryParams);
  }

  @Override
  public String toString() {
    return "Router[" + root.toString() + "]";
  }
}
