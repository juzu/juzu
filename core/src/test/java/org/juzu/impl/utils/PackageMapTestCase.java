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

import junit.framework.TestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PackageMapTestCase extends TestCase
{

   public void testResolve()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("foo", "foo_value");
      assertEquals("foo_value", pm.resolveValue("foo"));
      assertEquals("foo_value", pm.resolveValue("foo.bar"));
      assertEquals(null, pm.resolveValue("foobar"));
   }

   public void testResolvePrefix()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("foo", "foo_value");
      pm.putValue("foo.bar", "foo_bar_value");
      assertEquals("foo_value", pm.resolveValue("foo"));
      assertEquals("foo_value", pm.resolveValue("foo.juu"));
      assertEquals("foo_bar_value", pm.resolveValue("foo.bar"));
      assertEquals("foo_bar_value", pm.resolveValue("foo.bar.juu"));
   }

   public void testResolveEmptyPackage()
   {
      PackageMap<String> pm = new PackageMap<String>();
      pm.putValue("", "value");
      assertEquals("value", pm.resolveValue(""));
      assertEquals("value", pm.resolveValue("foo"));
      assertEquals("value", pm.resolveValue("foo.bar"));
   }
}
