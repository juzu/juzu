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

package juzu.impl.metamodel;

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelTestCase extends AbstractTestCase {
  
  /** . */
  private static final Key<MetaModelObject> A = Key.of("a", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> B = Key.of("b", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> C = Key.of("c", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> D = Key.of("d", MetaModelObject.class);

  /** . */
  private Context context;

  @Override
  public void setUp() throws Exception {
    this.context = new Context();
  }

  @Test
  public void testCannotRemoveRoot() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    a.addChild(B, b);

    //
    a.remove();
    assertSame(b, a.getChild(B));
    context.assertEmpty();
  }

  @Test
  public void testTransitiveRemove() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    Simple c = context.create("c");
    a.addChild(B, b).addChild(C, c);

    //
    b.remove();
    context.assertPreDetach("c");
    context.assertRemove("c");
    context.assertPreDetach("b");
    context.assertRemove("b");
    context.assertEmpty();
  }

  @Test
  public void testTransitiveRemoveChild() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    Simple c = context.create("c");
    a.addChild(B, b).addChild(C, c);

    //
    a.removeChild(B);
    context.assertPreDetach("c");
    context.assertRemove("c");
    context.assertPreDetach("b");
    context.assertRemove("b");
    context.assertEmpty();
  }

  @Test
  public void testRemoveOrphan() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    Simple c = context.create("c");
    a.addChild(C, c);
    b.addChild(C, c);

    //
    a.removeChild(C);
    context.assertPreDetach("c");
    context.assertEmpty();
    b.removeChild(C);
    context.assertPreDetach("c");
    context.assertRemove("c");
    context.assertEmpty();
  }

  @Test
  public void testBug() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    Simple c = context.create("c");
    Simple d = context.create("d");
    a.addChild(C, c);
    b.addChild(C, c);
    c.addChild(D, d);

    //
    assertSame(c, a.removeChild(C));
    assertNull(a.getChild(C));
    assertSame(c, b.getChild(C));
    assertSame(d, c.getChild(D));
  }

  @Test
  public void testEventWhenRemoved() {
    Simple a = context.create("a");
    Simple b = context.create("b");
    a.addChild(B, b);

  }

  static class Simple extends MetaModelObject {

    /** . */
    final Context context;
    
    /** . */
    final String name;

    Simple(Context context, String name) {
      this.context = context;
      this.name = name;
    }

    @Override
    protected void preDetach(MetaModelObject parent) {
      context.addLast(new Event(this, Event.PRE_DETACH));
    }

    @Override
    protected void preRemove() {
      context.addLast(new Event(this, Event.REMOVED));
    }

    @Override
    public String toString() {
      return "Simple[" + name + "]";
    }
  }
  
  static class Event {

    /** . */
    static final int ADDED = 0;

    /** . */
    static final int REMOVED = 1;

    /** . */
    static final int PRE_DETACH = 2;

    /** . */
    private final Simple source;

    /** . */
    private final int kind;

    Event(Simple source, int kind) {
      this.source = source;
      this.kind = kind;
    }
  }

  static class Context extends LinkedList<Event> {

    Simple create(String name) {
      return new Simple(this, name);
    }

    void assertRemove(String name) {
      assertTrue("Expecting to have at least one event", size() > 0);
      Event event = removeFirst();
      assertEquals(Event.REMOVED, event.kind);
      assertEquals(name, event.source.name);
    }

    void assertPreDetach(String name) {
      assertTrue("Expecting to have at least one event", size() > 0);
      Event event = removeFirst();
      assertEquals(Event.PRE_DETACH, event.kind);
      assertEquals(name, event.source.name);
    }

    void assertEmpty() {
      assertTrue(isEmpty());
    }
  }
}
