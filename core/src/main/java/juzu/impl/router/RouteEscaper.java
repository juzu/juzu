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

import juzu.impl.router.regex.GroupType;
import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REVisitor;

/**
 * <p>The route escaper transformer a regular expression with the following rules:</p>
 * <ul>
 *   <li>substitute any char occurence of the source <i>s</i> by the destination <i>d</i></li>
 *   <li>replace the <i>any</i> by the negated destination character <i>[^]</i></li> <li>append <i>&&[^s]</i> to
 *   any top character class</li>
 * </ul>
 * <p/>A few examples with <i>/</i> replaced by <i>_</i>:<p/>
 * <ul>
 *   <li><i>/</i> becomes <i>_</i>
 *   </li> <li><i>.</i> becomes <i>[^/]</i></li>
 *   <li><i>[a/]</i> becomes <i>[a_&[^/]]</i></li>
 *   <li><i>[,-1]</i> becomes <i>[,-.0-1_&&[^/]]</i></li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class RouteEscaper extends REVisitor<MalformedRouteException> {

  /** . */
  private final char src;

  /** . */
  private final char dst;

  RouteEscaper(char src, char dst) {
    this.src = src;
    this.dst = dst;
  }

  @Override
  protected void visit(RENode.Char expr) throws MalformedRouteException {
    if (expr.getValue() == src) {
      expr.setValue(dst);
    }
  }

  @Override
  protected void visit(RENode.Group expr) throws MalformedRouteException {
    if (expr.getType() == GroupType.CAPTURING_GROUP) {
      expr.setType(GroupType.NON_CAPTURING_GROUP);
    }
    super.visit(expr);
  }

  @Override
  protected void visit(RENode.Any expr) throws MalformedRouteException {
    RENode.CharacterClass repl = new RENode.CharacterClass(new RENode.CharacterClassExpr.Not(new RENode.CharacterClassExpr.Char('/')));
    repl.setQuantifier(expr.getQuantifier());
    expr.replaceBy(repl);
  }

  @Override
  protected void visit(RENode.CharacterClass expr) throws MalformedRouteException {
    RENode.CharacterClassExpr ccExpr = expr.getExpr();
    ccExpr = ccExpr.replace(src, dst);
//         RENode.CharacterClassExpr.And ccRepl = new RENode.CharacterClassExpr.And(null, new RENode.CharacterClassExpr.Not(new RENode.CharacterClassExpr.Char('/')));
//         ccExpr.replaceBy(ccRepl);
//         ccRepl.setLeft(ccExpr);
  }
}
