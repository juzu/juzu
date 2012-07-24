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

import java.io.IOException;

/**
 * Renders a {@link RENode} to its pattern representation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RERenderer extends REVisitor<IOException> {

  public static <A extends Appendable> A render(RENode re, A appendable) throws IOException, NullPointerException {
    if (re == null) {
      throw new NullPointerException("No null disjunction accepted");
    }
    if (appendable == null) {
      throw new NullPointerException("No null appendable accepted");
    }

    //
    RERenderer renderer = new RERenderer(appendable);

    //
    re.accept(renderer);

    //
    return appendable;
  }

  /** . */
  private final Appendable appendable;

  public RERenderer(Appendable appendable) {
    this.appendable = appendable;
  }

  protected void visit(RENode.Disjunction disjunction) throws IOException, NullPointerException {
    RENode.Alternative alternative = disjunction.getAlternative();
    if (alternative != null) {
      alternative.accept(this);
    }
    if (disjunction.hasAlternative() && disjunction.hasNext()) {
      appendable.append('|');
    }
    RENode.Disjunction next = disjunction.getNext();
    if (next != null) {
      next.accept(this);
    }
  }

  protected void visit(RENode.Alternative alternative) throws IOException, NullPointerException {
    alternative.getExpr().accept(this);
    RENode.Alternative next = alternative.getNext();
    if (next != null) {
      visit(next);
    }
  }

  protected void visit(RENode.Assertion.Begin expr) throws IOException {
    appendable.append('^');
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.Assertion.End expr) throws IOException {
    appendable.append('$');
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.Group expr) throws IOException {
    appendable.append(expr.getType().getOpen());
    this.visit(expr.getDisjunction());
    appendable.append(expr.getType().getClose());
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.Char expr) throws IOException {
    Literal.escapeTo(expr.getValue(), appendable);
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.Any expr) throws IOException {
    appendable.append('.');
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.CharacterClass expr) throws IOException {
    appendable.append("[");
    expr.getExpr().accept(this);
    appendable.append("]");
    if (expr.getQuantifier() != null) {
      expr.getQuantifier().toString(appendable);
    }
  }

  protected void visit(RENode.CharacterClassExpr.Not expr) throws IOException {
    boolean needBrace = false;
    for (RENode current = expr.getParent();current != null;current = current.getParent()) {
      if (current instanceof RENode.CharacterClassExpr.Or) {
        needBrace = true;
        break;
      }
      else if (current instanceof RENode.CharacterClassExpr.And) {
        needBrace = true;
        break;
      }
      else if (current instanceof RENode.CharacterClassExpr.Not) {
        needBrace = true;
        break;
      }
    }
    if (needBrace) {
      appendable.append("[");
    }
    appendable.append("^");
    expr.getNegated().accept(this);
    if (needBrace) {
      appendable.append(']');
    }
  }

  protected void visit(RENode.CharacterClassExpr.Or expr) throws IOException {
    expr.getLeft().accept(this);
    expr.getRight().accept(this);
  }

  protected void visit(RENode.CharacterClassExpr.And expr) throws IOException {
    expr.getLeft().accept(this);
    appendable.append("&&");
    expr.getRight().accept(this);
  }

  protected void visit(RENode.CharacterClassExpr.Range expr) throws IOException {
    visit(expr.getFrom());
    appendable.append('-');
    visit(expr.getTo());
  }

  protected void visit(RENode.CharacterClassExpr.Char expr) throws IOException {
    Literal.escapeTo(expr.getValue(), appendable);
  }
}
