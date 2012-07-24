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

import juzu.impl.compiler.ProcessingContext;
import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelTestCase extends AbstractTestCase {

  /** . */
  private static final Key<MetaModelObject> A = Key.of("a", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> B = Key.of("b", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> C = Key.of("c", MetaModelObject.class);

  /** . */
  private static final Key<MetaModelObject> D = Key.of("d", MetaModelObject.class);

  @Test
  public void testCannotRemoveRoot() {
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    a.addChild(B, b);

    //
    a.remove();
    assertSame(b, a.getChild(B));
    assertEquals(0, a.removed);
    assertEquals(0, b.removed);
  }

  @Test
  public void testTransitiveRemove() {
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    Simple c = new Simple("c");
    a.addChild(B, b).addChild(C, c);

    //
    b.remove();
    assertEquals(0, a.removed);
    assertEquals(1, b.removed);
    assertEquals(1, c.removed);
  }

  @Test
  public void testTransitiveRemoveChild() {
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    Simple c = new Simple("c");
    a.addChild(B, b).addChild(C, c);

    //
    a.removeChild(B);
    assertEquals(0, a.removed);
    assertEquals(1, b.removed);
    assertEquals(1, c.removed);
  }

  @Test
  public void testRemoveOrphan() {
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    Simple c = new Simple("c");
    a.addChild(C, c);
    b.addChild(C, c);

    //
    a.removeChild(C);
    assertEquals(0, c.removed);
    b.removeChild(C);
    assertEquals(1, c.removed);
  }

  @Test
  public void testTransitiveGarbage() {
    MetaModel m = new MetaModel();
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    m.addChild(A, a).addChild(B, b);

    //
    a.exist = b.exist = false;
    m.postActivate((ProcessingContext)null);
    assertNull(a.getChild(B));
    assertNull(m.getChild(B));
    assertEquals(1, a.removed);
    assertEquals(1, b.removed);
  }

  @Test
  public void testForcedGarbage() {
    MetaModel m = new MetaModel();
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    m.addChild(A, a).addChild(B, b);

    //
    a.exist = false;
    m.postActivate((ProcessingContext)null);
    assertNull(a.getChild(B));
    assertNull(m.getChild(A));
    assertEquals(1, a.removed);
    assertEquals(1, b.removed);
  }

  @Test
  public void testForcedGarbage2() {
    MetaModel m = new MetaModel();
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    Simple c = new Simple("c");
    m.addChild(A, a).addChild(B, b);
    m.addChild(C, c).addChild(B, b);

    //
    a.exist = false;
    m.postActivate((ProcessingContext)null);
    assertNull(a.getChild(B));
    assertNull(m.getChild(A));
    assertSame(b, c.getChild(B));
    assertEquals(1, a.removed);
    assertEquals(0, b.removed);
    assertEquals(0, c.removed);
    c.exist = false;
    m.postActivate((ProcessingContext)null);
    assertEquals(1, a.removed);
    assertEquals(1, b.removed);
    assertEquals(1, c.removed);
  }

  @Test
  public void testBug() {
    Simple a = new Simple("a");
    Simple b = new Simple("b");
    Simple c = new Simple("c");
    Simple d = new Simple("d");
    a.addChild(C, c);
    b.addChild(C, c);
    c.addChild(D, d);

    //
    assertSame(c, a.removeChild(C));
    assertNull(a.getChild(C));
    assertSame(c, b.getChild(C));
    assertSame(d, c.getChild(D));
  }

  static class Simple extends MetaModelObject {

    /** . */
    final String name;

    /** . */
    boolean exist = true;

    /** . */
    int removed = 0;

    Simple(String name) {
      this.name = name;
    }

    @Override
    public boolean exist(MetaModel model) {
      return exist;
    }

    @Override
    protected void preRemove() {
      removed++;
    }

    @Override
    public String toString() {
      return "Simple[" + name + "]";
    }
  }
}
