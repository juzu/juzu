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
import juzu.impl.router.regex.RERenderer;
import juzu.impl.router.regex.REVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ValueResolverFactory extends REVisitor<RuntimeException> {

  static class Alternative {

    /** . */
    private StringBuilder resolvingExpression = new StringBuilder();

    /** . */
    private String prefix;

    /** . */
    private String suffix;

    /** . */
    private StringBuilder buffer = new StringBuilder();

    /** . */
    private StringBuilder valueMatcher = new StringBuilder();

    StringBuilder getResolvingExpression() {
      return resolvingExpression;
    }

    String getPrefix() {
      return prefix;
    }

    String getSuffix() {
      return suffix;
    }

    StringBuilder getValueMatcher() {
      return valueMatcher;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[" + resolvingExpression + "]";
    }
  }

  /** . */
  private List<Alternative> alternatives = new ArrayList<Alternative>();

  /** . */
  private Alternative current = null;

  List<Alternative> foo(RENode root) {
    alternatives.clear();
    root.accept(this);
    return alternatives;
  }

  @Override
  protected void visit(RENode.Disjunction disjunction) throws RuntimeException {
    if (current != null) {
      RENode.Alternative alternative = disjunction.getAlternative();
      if (alternative != null) {
        alternative.accept(this);
      }
    }
    else {
      RENode.Alternative alternative = disjunction.getAlternative();
      if (alternative != null) {
        current = new Alternative();
        alternative.accept(this);
        current.suffix = current.buffer.toString();
        current.buffer.setLength(0);
        alternatives.add(current);
        current = null;
      }

      //
      RENode.Disjunction next = disjunction.getNext();
      if (next != null) {
        next.accept(this);
      }
    }
  }

  @Override
  protected void visit(RENode.Group expr) throws RuntimeException {
    if (expr.getType() == GroupType.CAPTURING_GROUP) {
      try {
        RERenderer renderer = new RERenderer(current.resolvingExpression);
        expr.accept(renderer);
      }
      catch (IOException e) {
        // Should not happen
        throw new AssertionError(e);
      }
      try {
        RERenderer renderer = new RERenderer(current.valueMatcher);
        expr.accept(renderer);
      }
      catch (IOException e) {
        // Should not happen
        throw new AssertionError(e);
      }
      current.prefix = current.buffer.toString();
      current.buffer.setLength(0);
    }
    else {
      super.visit(expr);
    }
  }

  @Override
  protected void visit(RENode.Alternative alternative) throws RuntimeException {
    alternative.getExpr().accept(this);
    RENode.Alternative next = alternative.getNext();
    if (next != null) {
      next.accept(this);
    }
  }

  @Override
  protected void visit(RENode.Char expr) throws RuntimeException {
    for (int i = expr.getMin();i > 0;i--) {
      current.resolvingExpression.append(expr.getValue());
      current.buffer.append(expr.getValue());
    }
  }

  @Override
  protected void visit(RENode.Any expr) throws RuntimeException {
    for (int i = expr.getMin();i > 0;i--) {
      // Any can be 'a'
      current.resolvingExpression.append('a');
      current.buffer.append('a');
    }
  }

  /** . */
  private Solver solver;

  @Override
  protected void visit(RENode.CharacterClass expr) throws RuntimeException {
    expr.getExpr().accept(this);
    for (int i = expr.getMin();i > 0;i--) {
      if (solver.hasNext()) {
        char c = solver.next();
        current.resolvingExpression.append(c);
        current.buffer.append(c);
        solver.reset();
      }
      else {
        throw new UnsupportedOperationException("wtf?");
      }
    }
  }

  @Override
  protected void visit(RENode.CharacterClassExpr.Or expr) throws RuntimeException {
    expr.getLeft().accept(this);
    Solver left = solver;
    expr.getRight().accept(this);
    Solver right = solver;
    solver = new Solver.Or(left, right);
  }

  @Override
  protected void visit(RENode.CharacterClassExpr.Range expr) throws RuntimeException {
    RENode.CharacterClassExpr.Char from = expr.getFrom();
    RENode.CharacterClassExpr.Char to = expr.getTo();
    solver = new Solver.Range(from.getValue(), to.getValue());
  }

  @Override
  protected void visit(RENode.CharacterClassExpr.Char expr) throws RuntimeException {
    solver = new Solver.Char(expr.getValue());
  }

  @Override
  protected void visit(RENode.CharacterClassExpr.And expr) throws RuntimeException {
    expr.getLeft().accept(this);
    Solver left = solver;
    expr.getRight().accept(this);
    Solver right = solver;
    solver = new Solver.And(left, right);
  }

  @Override
  protected void visit(RENode.CharacterClassExpr.Not expr) throws RuntimeException {
    RENode.CharacterClassExpr negated = expr.getNegated();
    if (negated == null) {
      // Do nothing ?
    }
    else {
      negate(negated);
    }
  }

  private void negate(RENode.CharacterClassExpr negated) throws RuntimeException {
    if (negated instanceof RENode.CharacterClassExpr.Not) {
      RENode.CharacterClassExpr nested = ((RENode.CharacterClassExpr.Not)negated).getNegated();
      if (nested != null) {
        nested.accept(this);
      }
    }
    else if (negated instanceof RENode.CharacterClassExpr.Or) {
      RENode.CharacterClassExpr.Or or = (RENode.CharacterClassExpr.Or)negated;
      negate(or.getLeft());
      Solver left = solver;
      negate(or.getRight());
      Solver right = solver;
      solver = new Solver.And(left, right);
    }
    else if (negated instanceof RENode.CharacterClassExpr.And) {
      RENode.CharacterClassExpr.And or = (RENode.CharacterClassExpr.And)negated;
      negate(or.getLeft());
      Solver left = solver;
      negate(or.getRight());
      Solver right = solver;
      solver = new Solver.Or(left, right);
    }
    else {
      char from;
      char to;
      if (negated instanceof RENode.CharacterClassExpr.Char) {
        from = to = ((RENode.CharacterClassExpr.Char)negated).getValue();
      }
      else if (negated instanceof RENode.CharacterClassExpr.Range) {
        RENode.CharacterClassExpr.Range range = (RENode.CharacterClassExpr.Range)negated;
        from = range.getFrom().getValue();
        to = range.getTo().getValue();
      }
      else {
        throw new UnsupportedOperationException();
      }
      Solver.Range left = null;
      Character c = prevValid(--from);
      if (c != null) {
        left = new Solver.Range(' ', c);
      }
      Solver.Range right = null;
      c = nextValid(++to);
      if (c != null) {
        right = new Solver.Range(c, Character.MAX_VALUE);
      }
      if (left == null) {
        if (right != null) {
          solver = right;
        }
      }
      else {
        if (right == null) {
          solver = left;
        }
        else {
          solver = new Solver.Or(left, right);
        }
      }
    }
  }

  private static abstract class Solver implements Iterator<Character> {

    public void remove() {
      throw new UnsupportedOperationException();
    }

    protected abstract void reset();

    private static class And extends Solver {

      /** . */
      private final Solver left;

      /** . */
      private final Solver right;

      /** . */
      private Character leftChar;

      /** . */
      private Character next;

      private And(Solver left, Solver right) {
        this.left = left;
        this.right = right;
        this.next = null;
        this.leftChar = null;
      }

      public boolean hasNext() {
        while (next == null) {
          if (leftChar == null) {
            if (left.hasNext()) {
              leftChar = left.next();
            }
            else {
              break;
            }
          }
          if (right.hasNext()) {
            Character c = right.next();
            if (c == leftChar) {
              next = c;
            }
          }
          else {
            right.reset();
            leftChar = null;
          }
        }
        return next != null;
      }

      public Character next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Character tmp = next;
        next = null;
        return tmp;
      }

      @Override
      protected void reset() {
        left.reset();
        right.reset();
      }
    }

    private static class Or extends Solver {

      /** . */
      private final Solver left;

      /** . */
      private final Solver right;

      private Or(Solver left, Solver right) {
        this.left = left;
        this.right = right;
      }

      public boolean hasNext() {
        return left.hasNext() || right.hasNext();
      }

      public Character next() {
        if (left.hasNext()) {
          return left.next();
        }
        else if (right.hasNext()) {
          return right.next();
        }
        throw new NoSuchElementException();
      }

      @Override
      protected void reset() {
        left.reset();
        right.reset();
      }
    }

    private static class Range extends Solver {

      /** . */
      private char from;

      /** . */
      private char current;

      /** . */
      private char to;

      private Range(char from, char to) {
        this.from = from;
        this.current = from;
        this.to = to;
      }

      public boolean hasNext() {
        return current < to;
      }

      public Character next() {
        if (current >= to) {
          throw new NoSuchElementException();
        }
        return current++;
      }

      @Override
      protected void reset() {
        current = from;
      }
    }

    private static class Char extends Solver {

      /** . */
      private final char value;

      /** . */
      private boolean done;

      private Char(char value) {
        this.value = value;
        this.done = false;
      }

      public boolean hasNext() {
        return !done;
      }

      public Character next() {
        if (done) {
          throw new NoSuchElementException();
        }
        done = true;
        return value;
      }

      @Override
      protected void reset() {
        done = false;
      }
    }
  }

  private static Character nextValid(char from) {
    while (true) {
      if (!Character.isISOControl(from)) {
        return from;
      }
      else {
        if (from == Character.MAX_VALUE) {
          return null;
        }
        else {
          from++;
        }
      }
    }
  }

  private static Character prevValid(char from) {
    while (true) {
      if (!Character.isISOControl(from)) {
        return from;
      }
      else {
        if (from == Character.MIN_VALUE) {
          return null;
        }
        else {
          from--;
        }
      }
    }
  }
}
