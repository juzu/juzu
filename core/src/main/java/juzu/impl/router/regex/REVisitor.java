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

package juzu.impl.router.regex;

/**
 * Renders a {@link juzu.impl.router.regex.RENode} to its pattern representation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class REVisitor<E extends Exception> {

  protected void visit(RENode.Disjunction disjunction) throws E {
    RENode alternative = disjunction.getAlternative();
    if (alternative != null) {
      alternative.accept(this);
    }
    RENode.Disjunction next = disjunction.getNext();
    if (next != null) {
      next.accept(this);
    }
  }

  protected void visit(RENode.Alternative alternative) throws E {
    alternative.getExpr().accept(this);
    RENode.Alternative next = alternative.getNext();
    if (next != null) {
      next.accept(this);
    }
  }

  protected void visit(RENode.Assertion.Begin expr) throws E {
  }

  protected void visit(RENode.Assertion.End expr) throws E {
  }

  protected void visit(RENode.Group expr) throws E {
    RENode.Disjunction disjunction = expr.getDisjunction();
    if (disjunction != null) {
      disjunction.accept(this);
    }
  }

  protected void visit(RENode.Char expr) throws E {
  }

  protected void visit(RENode.Any expr) throws E {
  }

  protected void visit(RENode.CharacterClass expr) throws E {
    expr.getExpr().accept(this);
  }

  protected void visit(RENode.CharacterClassExpr.Not expr) throws E {
    RENode.CharacterClassExpr negated = expr.getNegated();
    if (negated != null) {
      negated.accept(this);
    }
  }

  protected void visit(RENode.CharacterClassExpr.Or expr) throws E {
    RENode.CharacterClassExpr left = expr.getLeft();
    if (left != null) {
      left.accept(this);
    }
    RENode.CharacterClassExpr right = expr.getRight();
    if (right != null) {
      right.accept(this);
    }
  }

  protected void visit(RENode.CharacterClassExpr.And expr) throws E {
    RENode.CharacterClassExpr left = expr.getLeft();
    if (left != null) {
      left.accept(this);
    }
    RENode.CharacterClassExpr right = expr.getRight();
    if (right != null) {
      right.accept(this);
    }
  }

  protected void visit(RENode.CharacterClassExpr.Range expr) throws E {
  }

  protected void visit(RENode.CharacterClassExpr.Char expr) throws E {
  }
}
