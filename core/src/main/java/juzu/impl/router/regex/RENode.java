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
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class RENode {

  /** The owner. */
  private Ref<?> owner;

  public abstract String toString();

  public abstract <E extends Exception> void accept(REVisitor<E> visitor) throws E;

  public final RENode getParent() {
    return owner != null ? owner.parent : null;
  }

  public final RENode getRoot() {
    RENode root = this;
    for (RENode parent = root.getParent();parent != null;parent = root.getParent()) {
      root = parent;
    }
    return root;
  }

  public final RENode replaceBy(RENode that) throws IllegalStateException {
    if (owner == null) {
      throw new IllegalStateException("Not attached");
    }
    return owner.replace(that);
  }

  public static final class Disjunction extends RENode {

    /** . */
    private NullableRef<Alternative> alternative;

    /** . */
    private NullableRef<Disjunction> next;

    public Disjunction() {
      this.alternative = null;
      this.next = null;
    }

    public Disjunction(Alternative alternative) {
      this.alternative = new NullableRef<Alternative>(this, Alternative.class, alternative);
      this.next = null;
    }

    public Disjunction(Alternative alternative, Disjunction next) {
      this.alternative = new NullableRef<Alternative>(this, Alternative.class, alternative);
      this.next = new NullableRef<Disjunction>(this, Disjunction.class, next);
    }

    public Disjunction(Disjunction next) {
      this.alternative = null;
      this.next = new NullableRef<Disjunction>(this, Disjunction.class, next);
    }

    public Alternative getAlternative() {
      return alternative != null ? alternative.get() : null;
    }

    public void setAlternative(Alternative alternative) {
      if (this.alternative == null) {
        this.alternative = new NullableRef<Alternative>(this, Alternative.class, alternative);
      }
      else {
        this.alternative.set(alternative);
      }
    }

    public boolean hasAlternative() {
      return alternative != null;
    }

    public void clearAlternative() {
      if (this.alternative != null) {
        this.alternative.set(null);
        this.alternative = null;
      }
    }

    public Disjunction getNext() {
      return next != null ? next.get() : null;
    }

    public void setNext(Disjunction next) {
      if (this.next == null) {
        this.next = new NullableRef<Disjunction>(this, Disjunction.class, next);
      }
      else {
        this.next.set(next);
      }
    }

    public boolean hasNext() {
      return next != null;
    }

    public void clearNext() {
      if (this.next != null) {
        this.next.set(null);
        this.next = null;
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (alternative != null) {
        if (alternative.isNotNull()) {
          sb.append(alternative.get());
        }
      }
      if (alternative != null && next != null) {
        sb.append('|');
      }
      if (next != null) {
        if (next.isNotNull()) {
          sb.append(next.get());
        }
      }
      return sb.toString();
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static final class Alternative extends RENode {

    /** . */
    private final Ref<Expr> expr;

    /** . */
    private final Ref<Alternative> next;

    public Alternative(Expr expr) {
      this(expr, null);
    }

    public Alternative(Expr expr, Alternative next) {
      this.expr = new NonNullableRef<Expr>(this, Expr.class, expr);
      this.next = new NullableRef<Alternative>(this, Alternative.class, next);
    }

    public Expr getExpr() {
      return expr.get();
    }

    public void setExpr(Expr expr) {
      this.expr.set(expr);
    }

    public Alternative getNext() {
      return next.get();
    }

    public void setNext(Alternative next) {
      this.next.set(next);
    }

    @Override
    public String toString() {
      if (next.isNotNull()) {
        return expr.get().toString() + next.get();
      }
      else {
        return expr.get().toString();
      }
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static abstract class Expr extends RENode {

    /** . */
    private Quantifier quantifier;

    private Expr() {
    }

    public final int getMin() {
      return quantifier == null ? 1 : quantifier.getMin();
    }

    public final Quantifier getQuantifier() {
      return quantifier;
    }

    public final void setQuantifier(Quantifier quantifier) {
      this.quantifier = quantifier;
    }

    @Override
    public final String toString() {
      StringBuilder sb = new StringBuilder();
      if (quantifier != null) {
        String q = quantifier.toString();
        sb.append('<').append(q).append('>');
        writeTo(sb);
        sb.append("</").append(q).append('>');
      }
      else {
        writeTo(sb);
      }
      return sb.toString();
    }

    protected abstract void writeTo(StringBuilder sb);
  }

  public static abstract class Assertion extends Expr {

    private Assertion() {
    }

    public static final class Begin extends Assertion {
      @Override
      protected void writeTo(StringBuilder sb) {
        sb.append("<^/>");
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }

    public static final class End extends Assertion {
      @Override
      protected void writeTo(StringBuilder sb) {
        sb.append("<$/>");
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }
  }

  public static final class Group extends Expr {

    /** . */
    private GroupType type;

    /** . */
    private final Ref<Disjunction> disjunction;

    public Group(Disjunction disjunction, GroupType type) throws NullPointerException {
      if (type == null) {
        throw new NullPointerException("No null type accepted");
      }
      this.disjunction = new NullableRef<Disjunction>(this, Disjunction.class, disjunction);
      this.type = type;
    }

    public Disjunction getDisjunction() {
      return disjunction.get();
    }

    public void setDisjunction(Disjunction disjunction) {
      this.disjunction.set(disjunction);
    }

    public GroupType getType() {
      return type;
    }

    public void setType(GroupType type) {
      this.type = type;
    }

    @Override
    protected void writeTo(StringBuilder sb) {
      sb.append("<").append(type.getOpen()).append('>').append(disjunction.get()).append("</").append(type.getOpen()).append(">");
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static abstract class Atom extends Expr {
    private Atom() {
    }
  }

  public static final class Any extends Atom {
    @Override
    protected void writeTo(StringBuilder sb) {
      sb.append("<./>");
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static final class Char extends Atom {

    /** . */
    private char value;

    public Char(char value) {
      this.value = value;
    }

    public char getValue() {
      return value;
    }

    public void setValue(char value) {
      this.value = value;
    }

    @Override
    protected void writeTo(StringBuilder sb) {
      sb.append("<c>").append(value).append("</c>");
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static class CharacterClass extends Atom {

    /** . */
    private final Ref<CharacterClassExpr> expr;

    public CharacterClass(CharacterClassExpr expr) {
      this.expr = new NonNullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, expr);
    }

    public CharacterClassExpr getExpr() {
      return expr.get();
    }

    public void setExpr(CharacterClassExpr expr) {
      this.expr.set(expr);
    }

    @Override
    protected void writeTo(StringBuilder sb) {
      sb.append(expr.get());
    }

    @Override
    public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
      visitor.visit(this);
    }
  }

  public static abstract class CharacterClassExpr extends RENode {

    private CharacterClassExpr() {
    }

    /**
     * Remove the specifed char from the expression.
     *
     * @param c the char to remove
     * @return the replacement for this node
     */
    public CharacterClassExpr remove(char c) {
      throw new UnsupportedOperationException();
    }

    /**
     * Remove the specifed char from the expression.
     *
     * @param src the char is substituted
     * @param dst the char that substitutes
     * @return the replacement for this node
     */
    public CharacterClassExpr replace(char src, char dst) {
      throw new UnsupportedOperationException();
    }

    public static class Not extends CharacterClassExpr {

      /** . */
      private final Ref<CharacterClassExpr> negated;

      public Not(CharacterClassExpr negated) {
        this.negated = new NullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, negated);
      }

      public CharacterClassExpr getNegated() {
        return negated.get();
      }

      public void setNegated(CharacterClassExpr negated) {
        this.negated.set(negated);
      }

      @Override
      public CharacterClassExpr remove(char c) {
        this.negated.get().remove(c);
        return this;
      }

      @Override
      public CharacterClassExpr replace(char src, char dst) {
        this.negated.get().replace(src, dst);
        return this;
      }

      @Override
      public String toString() {
        return "[^" + negated.get() + "]";
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }

    public static class Or extends CharacterClassExpr {

      /** . */
      private final Ref<CharacterClassExpr> left;

      /** . */
      private final Ref<CharacterClassExpr> right;

      public Or(CharacterClassExpr left, CharacterClassExpr right) {
        this.left = new NullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, left);
        this.right = new NullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, right);
      }

      public CharacterClassExpr getLeft() {
        return left.get();
      }

      public void setLeft(CharacterClassExpr left) {
        this.left.set(left);
      }

      public CharacterClassExpr getRight() {
        return right.get();
      }

      public void setRight(CharacterClassExpr right) {
        this.right.set(right);
      }

      @Override
      public CharacterClassExpr remove(char c) {
        if (left.isNotNull()) {
          left.get().remove(c);
        }
        if (right.isNotNull()) {
          right.get().remove(c);
        }
        return this;
      }

      @Override
      public CharacterClassExpr replace(char src, char dst) {
        if (left.isNotNull()) {
          left.get().replace(src, dst);
        }
        if (right.isNotNull()) {
          right.get().replace(src, dst);
        }
        return this;
      }

      @Override
      public String toString() {
        String l = left.isNotNull() ? left.get().toString() : "";
        String r = right.isNotNull() ? right.get().toString() : "";
        return "[" + l + "||" + r + "]";
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }

    public static class And extends CharacterClassExpr {

      /** . */
      private final Ref<CharacterClassExpr> left;

      /** . */
      private final Ref<CharacterClassExpr> right;

      public And(CharacterClassExpr left, CharacterClassExpr right) {
        this.left = new NullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, left);
        this.right = new NullableRef<CharacterClassExpr>(this, CharacterClassExpr.class, right);
      }

      public CharacterClassExpr getLeft() {
        return left.get();
      }

      public void setLeft(CharacterClassExpr left) {
        this.left.set(left);
      }

      public CharacterClassExpr getRight() {
        return right.get();
      }

      public void setRight(CharacterClassExpr right) {
        this.right.set(right);
      }

      @Override
      public CharacterClassExpr remove(char c) {
        if (left.isNotNull()) {
          left.get().remove(c);
        }
        if (right.isNotNull()) {
          right.get().remove(c);
        }
        return this;
      }

      @Override
      public CharacterClassExpr replace(char src, char dst) {
        if (left.isNotNull()) {
          left.get().replace(src, dst);
        }
        if (right.isNotNull()) {
          right.get().replace(src, dst);
        }
        return this;
      }

      @Override
      public String toString() {
        String l = left.isNotNull() ? left.get().toString() : "";
        String r = right.isNotNull() ? right.get().toString() : "";
        return "[" + l + "&&" + r + "]";
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }

    public static class Char extends CharacterClassExpr {

      /** . */
      private char value;

      public Char(char value) {
        this.value = value;
      }

      public char getValue() {
        return value;
      }

      public void setValue(char value) {
        this.value = value;
      }

      @Override
      public CharacterClassExpr remove(char c) {
        if (c == value) {
          replaceBy(null);
          return null;
        }
        else {
          return this;
        }
      }

      @Override
      public CharacterClassExpr replace(char src, char dst) {
        if (src == value) {
          value = dst;
        }
        return this;
      }

      @Override
      public String toString() {
        return "[" + value + "]";
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }

    public static class Range extends CharacterClassExpr {

      /** From inclusive. */
      private RENode.CharacterClassExpr.Char from;

      /** To inclusive. */
      private RENode.CharacterClassExpr.Char to;

      public Range(RENode.CharacterClassExpr.Char from, RENode.CharacterClassExpr.Char to) {
        if (from.value > to.value) {
          throw new IllegalArgumentException("From cannot be greater or equals than to");
        }
        this.from = from;
        this.to = to;
      }

      public CharacterClassExpr remove(char c) throws IllegalArgumentException {
        if (from.value == to.value) {
          if (from.value == c) {
            throw new UnsupportedOperationException();
          }
        }
        else if (from.value + 1 == to.value) {
          if (from.value == c) {
            Char repl = new Char(to.value);
            replaceBy(repl);
            return repl;
          }
          else {
            Char repl = new Char(from.value);
            replaceBy(repl);
            return repl;
          }
        }
        else {
          if (from.value == c) {
            from.value++;
          }
          else if (to.value == c) {
            to.value--;
          }
          else if (from.value < c && c < to.value) {
            CharacterClassExpr left;
            if (from.value + 1 == c) {
              left = new Char(from.value);
            }
            else {
              left = new Range(from, new Char((char)(c - 1)));
            }
            CharacterClassExpr right;
            if (c == to.value - 1) {
              right = new Char(to.value);
            }
            else {
              right = new Range(new Char((char)(c + 1)), to);
            }
            Or repl = new Or(left, right);
            replaceBy(repl);
            return repl;
          }
        }

        // We keep the same node
        return this;
      }

      @Override
      public CharacterClassExpr replace(char src, char dst) {
        CharacterClassExpr repl = remove(src);
        if (repl != this) {
          Or or = new Or(null, new Char(dst));
          repl.replaceBy(or);
          or.setLeft(repl);
          repl = or;
        }
        return repl;
      }

      public RENode.CharacterClassExpr.Char getFrom() {
        return from;
      }

      public RENode.CharacterClassExpr.Char getTo() {
        return to;
      }

      @Override
      public String toString() {
        return "[" + from.value + "-" + to.value + "]";
      }

      @Override
      public <E extends Exception> void accept(REVisitor<E> visitor) throws E {
        visitor.visit(this);
      }
    }
  }

  protected abstract class Ref<N extends RENode> {

    /** . */
    private final Class<N> type;

    /** . */
    private final RENode parent;

    protected Ref(RENode parent, Class<N> type) {
      this.parent = parent;
      this.type = type;
    }

    public final Class<N> getType() {
      return type;
    }

    protected abstract N set(N node);

    protected abstract N get();

    protected final boolean isNull() {
      return get() == null;
    }

    protected final boolean isNotNull() {
      return get() != null;
    }

    protected final N replace(RENode that) {
      if (that == null || type.isInstance(that)) {
        return set(type.cast(that));
      }
      else {
        throw new ClassCastException("Cannot cast node with type " + that.getClass().getName() + " to type " +
            type.getName());
      }
    }

  }

  protected class NullableRef<N extends RENode> extends Ref<N> {

    /** . */
    private N node;

    public NullableRef(RENode parent, Class<N> type) {
      this(parent, type, null);
    }

    public NullableRef(RENode parent, Class<N> type, N node) {
      super(parent, type);

      //
      if (node != null) {
        if (node.owner != null) {
          throw new IllegalArgumentException();
        }
        else {
          node.owner = this;
        }
      }
      this.node = node;
    }

    @Override
    protected N set(N node) {
      if (node != null && node.owner != null) {
        throw new IllegalArgumentException();
      }
      N previous = this.node;
      if (this.node != null) {
        this.node.owner = null;
      }
      if (node != null) {
        node.owner = this;
        this.node = node;
      }
      else {
        this.node = null;
      }
      return previous;
    }

    @Override
    protected N get() {
      return node;
    }
  }

  protected class NonNullableRef<N extends RENode> extends Ref<N> {

    /** . */
    private N node;

    public NonNullableRef(RENode parent, Class<N> type, N node) {
      super(parent, type);

      //
      if (node == null) {
        throw new NullPointerException("No null node accepted");
      }
      if (node.owner != null) {
        throw new IllegalArgumentException();
      }
      node.owner = this;
      this.node = node;
    }

    @Override
    protected N set(N node) {
      if (node == null) {
        throw new NullPointerException("No null node accepted");
      }
      if (node.owner != null) {
        throw new IllegalArgumentException();
      }
      N previous = this.node;
      this.node.owner = null;
      node.owner = this;
      this.node = node;
      return previous;
    }

    @Override
    protected N get() {
      return node;
    }
  }
}
