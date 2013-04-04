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
