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

import juzu.impl.router.regex.RE;
import juzu.impl.router.regex.REFactory;
import juzu.impl.common.Tools;

import java.util.BitSet;

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
    super(null, Route.TERMINATION_NONE);

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

  @Override
  public String toString() {
    return "Router[" + super.toString() + "]";
  }
}
