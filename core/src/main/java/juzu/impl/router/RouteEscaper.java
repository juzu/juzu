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
