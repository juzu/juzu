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

package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpliteratorTestCase extends AbstractTestCase {

  @Test
  public void testEmptyString() {
    Spliterator i = new Spliterator("", '.');
    assertTrue(i.hasNext());
    assertEquals("", i.next());
    assertFalse(i.hasNext());
  }

  @Test
  public void testSimpleString() {
    Spliterator i = new Spliterator("a", '.');
    assertTrue(i.hasNext());
    assertEquals("a", i.next());
    assertFalse(i.hasNext());
  }

  @Test
  public void testSeparatorString() {
    Spliterator i = new Spliterator(".", '.');
    assertTrue(i.hasNext());
    assertEquals("", i.next());
    assertTrue(i.hasNext());
    assertEquals("", i.next());
    assertFalse(i.hasNext());
  }

  @Test
  public void testEntireString() {
    Spliterator i = new Spliterator("a", '.');
    assertTrue(i.hasNext());
    assertEquals("a", i.next());
    assertFalse(i.hasNext());
  }

  @Test
  public void testNormal() {
    Spliterator i = new Spliterator("a.b", '.');
    assertTrue(i.hasNext());
    assertEquals("a", i.next());
    assertTrue(i.hasNext());
    assertEquals("b", i.next());
    assertFalse(i.hasNext());
  }

  @Test
  public void testBounds() {
    for (int[] range : new int[][]{{0,2},{-1,1},{1,0}}) {
      try {
        new Spliterator("a", range[0], range[1], '.');
        fail();
      }
      catch (IndexOutOfBoundsException e) {
      }
    }
  }
}
