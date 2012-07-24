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

package juzu.impl.template.spi.juzu.ast;

import juzu.impl.common.Coordinate;
import juzu.impl.common.Location;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Tools;
import juzu.template.TagHandler;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ASTNode<N extends ASTNode<N>> implements Serializable {

  /** . */
  private static final Coordinate DUMB = new Coordinate(0, new Location(1, 1));

  /** . */
  private final Location beginPosition;

  /** . */
  protected final List<Block<?>> children;

  /** . */
  private final List<Block<?>> unmodifiableChildren;

  protected ASTNode(Location beginPosition, List<ASTNode.Block<?>> children) {
    if (beginPosition == null) {
      throw new NullPointerException("No null position accepted");
    }
    this.beginPosition = beginPosition;
    this.children = children;
    this.unmodifiableChildren = children != null ? Collections.unmodifiableList(children) : Collections.<Block<?>>emptyList();
  }

  public Location getBeginPosition() {
    return beginPosition;
  }

  public List<Block<?>> getChildren() {
    return unmodifiableChildren;
  }

  public N addChildren(Iterable<Block<?>> children) {
    for (Block child : children) {
      addChild(child);
    }
    return (N)this;
  }

  public N addChild(Block<?> child) {
    if (children == null) {
      throw new IllegalStateException("Node " + this + " cannot have children");
    }
    if (child.parent != null) {
      child.parent.children.remove(child);
      child.parent = null;
    }
    child.parent = this;
    children.add(child);
    return (N)this;
  }

  public static class Template extends ASTNode<Template> {

    public static Template parse(CharSequence s) throws ParseException {
      // At this point we could use something like a CharSequenceReader class or something
      TemplateParser parser = new TemplateParser(new OffsetTokenManager(new OffsetCharStream(new OffsetReader(new StringReader(s.toString())))));
      return parser.parse();
    }

    public Template() {
      super(new Location(0, 0), new ArrayList<Block<?>>());
    }


  }

  public abstract static class Block<B extends Block<B>> extends ASTNode<B> {

    /** . */
    private final Coordinate begin;

    /** . */
    private final Coordinate end;

    /** . */
    private ASTNode<?> parent;

    protected Block(Coordinate begin, Coordinate end, List<Block<?>> children) {
      super(begin.getPosition(), children);

      //
      this.begin = begin;
      this.end = end;
      this.parent = null;
    }

    public ASTNode<?> getParent() {
      return parent;
    }

    public void addAfter(Block sibling) {
      if (sibling.parent != null) {
        sibling.parent.children.remove(sibling);
        sibling.parent = null;
      }
      int index = parent.children.indexOf(this);
      parent.children.add(index + 1, sibling);
      sibling.parent = parent;
    }

    public void remove() throws IllegalStateException {
      if (parent == null) {
        throw new IllegalStateException("No parent");
      }
      parent.children.remove(this);
      parent = null;
    }

    public Coordinate getBegin() {
      return begin;
    }

    public Coordinate getEnd() {
      return end;
    }

    public int getBeginOffset() {
      return begin.getOffset();
    }

    public int getEndOffset() {
      return end.getOffset();
    }

    public Location getEndPosition() {
      return end.getPosition();
    }
  }

  public static class Tag extends Block<Tag> {

    /** The tag name. */
    private final String name;

    /** . */
    private final Map<String, String> args;

    /** . */
    private transient TagHandler handler;

    public Tag(String name) {
      this(name, Collections.<String, String>emptyMap());
    }

    public Tag(String name, Map<String, String> args) {
      this(DUMB, DUMB, name, args);
    }

    public Tag(Coordinate begin, Coordinate end, String name, Map<String, String> args) {
      super(begin, end, new ArrayList<Block<?>>());

      //
      this.name = name;
      this.args = args;
    }

    public String getName() {
      return name;
    }

    public Map<String, String> getArgs() {
      return args;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Tag) {
        Tag that = (Tag)obj;
        return name.equals(name) && args.equals(that.args) && children.equals(that.children);
      }
      return false;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[name=" + name + ",args=" + args + "]";
    }
  }

  public static class URL extends Block<URL> {

    /** . */
    private final String typeName;

    /** . */
    private final String methodName;

    /** . */
    private final Map<String, String> args;

    /** . */
    private MethodInvocation invocation;

    public URL(String typeName, String methodName, Map<String, String> args) {
      this(DUMB, DUMB, typeName, methodName, args);
    }

    public URL(Coordinate begin, Coordinate end, String typeName, String methodName, Map<String, String> args) {
      super(begin, end, null);

      //
      this.typeName = typeName;
      this.methodName = methodName;
      this.args = args;
      this.invocation = null;
    }

    public String getTypeName() {
      return typeName;
    }

    public String getMethodName() {
      return methodName;
    }

    public Map<String, String> getArgs() {
      return args;
    }

    public MethodInvocation getInvocation() {
      return invocation;
    }

    public void setInvocation(MethodInvocation invocation) {
      this.invocation = invocation;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof URL) {
        URL that = (URL)obj;
        return Tools.safeEquals(typeName, that.typeName) && methodName.equals(that.methodName) && args.equals(that.args);
      }
      return false;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[name=" + methodName + ",args=" + args + "]";
    }
  }

  public static class Section extends Block<Section> {

    /** . */
    private final SectionType type;

    /** . */
    private final String text;

    public Section(SectionType type, String text) {
      this(DUMB, DUMB, type, text);
    }

    public Section(Coordinate begin, Coordinate end, SectionType type, String text) {
      super(begin, end, null);

      //
      if (type == null) {
        throw new NullPointerException();
      }
      if (text == null) {
        throw new NullPointerException();
      }


      //
      this.text = text;
      this.type = type;
    }

    public SectionType getType() {
      return type;
    }

    public String getText() {
      return text;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Section) {
        Section that = (Section)obj;
        return type == that.type && text.equals(that.text);
      }
      return false;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[type=" + type + ",text=" + text + "]";
    }
  }

}
