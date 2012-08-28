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
import juzu.impl.router.regex.RE;
import juzu.impl.router.regex.REFactory;
import juzu.impl.common.MimeType;
import juzu.impl.common.QualifiedName;
import juzu.impl.common.Tools;

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
public class Router extends Route {

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
  private final REFactory factory;

  /** . */
  private RERef[] regexes;

  /** The slash escape char. */
  final char separatorEscape;

  /** . */
  final char separatorEscapeNible1;

  /** . */
  final char separatorEscapeNible2;

  public Router() throws RouterConfigException {
    this('_', REFactory.JAVA);
  }

  public Router(char separatorEscape) throws RouterConfigException {
    this(separatorEscape, REFactory.JAVA);
  }

  public Router(char separatorEscape, REFactory regexFactory) throws RouterConfigException {
    super(null);

    //
    int i = separatorEscape & ~0x7F;
    if (i > 0 || !escapeSet.get(separatorEscape)) {
      throw new RouterConfigException("Char " + (int)separatorEscape + " cannot be used a separator escape");
    }

    //
    String s = Integer.toString(separatorEscape, 16).toUpperCase();
    separatorEscapeNible1 = s.charAt(0);
    separatorEscapeNible2 = s.charAt(1);

    //
    this.separatorEscape = separatorEscape;
    this.regexes = new RERef[0];
    this.factory = regexFactory;
  }

  RERef compile(String pattern) {
    for (RERef regex : regexes) {
      if (regex.re.getPattern().equals(pattern)) {
        return regex;
      }
    }
    //
    RE regex = factory.compile(pattern);
    RERef holder = new RERef(regexes.length, regex);
    regexes = Tools.appendTo(regexes, holder);
    return holder;
  }

  public void render(Map<QualifiedName, String> parameters, URIWriter writer) throws IOException {
    render(new RenderContext(parameters), writer);
  }

  public String render(Map<QualifiedName, String> parameters) {
    return render(new RenderContext(parameters));
  }

  public void render(RenderContext context, URIWriter writer) throws IOException {
    bilto(context);

    // Ok, so this is not the fastest way to do it, but for now it's OK, it's what is needed, we'll find
    // a way to optimize it later with some precompilation.
    RouteMatch r = resolve(context);

    // We found a route we need to render it now
    if (r != null) {
      r.render(writer);
    }
  }

  void bilto(RenderContext context) {
    if (context.matchers == null || context.matchers.length < regexes.length) {
      context.matchers = new RE.Matcher[regexes.length];
    }
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

  @Override
  public String toString() {
    return "Router[" + super.toString() + "]";
  }
}
