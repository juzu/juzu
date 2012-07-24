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

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BitStackTestCase extends AbstractTestCase {

  @Test
  public void testSimple() {
    BitStack<Boolean> bs = new BitStack<Boolean>();
    assertEquals(0, bs.getDepth());
    bs.init(2);
    bs.push();
    assertEquals(1, bs.getDepth());
    bs.set(1, true);
    assertFalse(bs.isEmpty());
    bs.push();
    assertEquals(2, bs.getDepth());
    bs.set(0, true);
    assertTrue(bs.isEmpty());
    bs.pop();
    assertEquals(1, bs.getDepth());
    assertFalse(bs.isEmpty());
    bs.pop();
    assertEquals(0, bs.getDepth());
  }

  @Test
  public void testReuse() {
    BitStack<Boolean> bs = new BitStack<Boolean>();
    bs.init(2);
    bs.push();
    bs.set(0, true);
    bs.push();
    bs.set(1, true);
    assertTrue(bs.isEmpty());
    bs.pop();
    bs.push();
    assertFalse(bs.isEmpty());
  }

  @Test
  public void testState() {
    BitStack<Boolean> bs = new BitStack<Boolean>();
    try {
      bs.set(0, true);
      fail();
    }
    catch (IllegalStateException e) {
    }
    try {
      bs.pop();
      fail();
    }
    catch (IllegalStateException e) {
    }
  }
}
