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

import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IterableArrayTestCase extends AbstractTestCase {

  @Test
  public void testSimple() {
    String[] a = {"a", "b"};
    assertEquals(Collections.<String>emptyList(), Tools.list(new IterableArray<String>(a, 0, 0)));
    assertEquals(Arrays.asList("a"), Tools.list(new IterableArray<String>(a, 0, 1)));
    assertEquals(Arrays.asList("a", "b"), Tools.list(new IterableArray<String>(a, 0, 2)));
    assertEquals(Arrays.asList("b"), Tools.list(new IterableArray<String>(a, 1, 2)));
  }
}
