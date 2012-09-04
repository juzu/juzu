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

import org.junit.Test;

import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RoutePriorityTestCase extends AbstractControllerTestCase {

  @Test
  public void testExactMatchingAfterWildcard() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/{foo}");
    Route b = router.append("/foo");

    router.assertRoute(a, "/foo");
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/b", a.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "b")).render());
  }

  @Test
  public void testExactMatchingBeforeWildcard() throws Exception {
    RouterAssert router = new RouterAssert();
    Route a = router.append("/foo");
    Route b = router.append("/{foo}");

    router.assertRoute(a, "/foo");
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", a.matches(Collections.singletonMap(Names.FOO, "foo")).render());
    assertEquals("/b", b.matches(Collections.singletonMap(Names.FOO, "b")).render());
    assertEquals("/foo", b.matches(Collections.singletonMap(Names.FOO, "foo")).render());
  }
}
