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
