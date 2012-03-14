/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.utils;

import org.junit.Test;
import org.juzu.test.AbstractTestCase;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathTestCase extends AbstractTestCase
{

   @Test
   public void testParse()
   {
      assertPath("");
      assertPath("/");
      assertPath("//");
      assertPath("a", "a");
      assertPath("/a", "a");
      assertPath("a/", "a");
      assertPath("a/b", "a", "b");
      assertPath("a//b", "a", "b");
   }

   @Test
   public void testNext()
   {
      Path a = Path.parse("a/b/c", '/');
      Path b = a.next();
      Path c = b.next();
      Path next = c.next();
      assertPath(b, "b", "c");
      assertPath(c, "c");
      assertPath(next);
      assertNull(next.next());
   }

   @Test
   public void testToString()
   {
      assertEquals("Path[]", Path.parse("", '/').toString());
      assertEquals("Path[a]", Path.parse("a", '/').toString());
      assertEquals("Path[a.b]", Path.parse("a/b", '/').toString());
   }

   private void assertPath(String path, String... test)
   {
      assertPath(Path.parse(path, '/'), test);
   }

   private void assertPath(Path path, String... test)
   {
      assertEquals(test.length, path.size());
      Iterator<String> iterator = path.iterator();
      for (int i = 0;i < test.length;i++)
      {
         assertEquals(test[i], path.get(i));
         assertTrue(iterator.hasNext());
         assertEquals(test[i], iterator.next());
      }
      assertFalse(iterator.hasNext());
   }
}
