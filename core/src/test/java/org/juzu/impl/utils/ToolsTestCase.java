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

import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ToolsTestCase extends TestCase
{

   public void testEmptyNoRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("", false);
      assertTrue(p.matcher("").matches());
      assertFalse(p.matcher("foo").matches());
      assertFalse(p.matcher("foo.bar").matches());
   }

   public void testEmptyRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("", true);
      assertTrue(p.matcher("").matches());
      assertTrue(p.matcher("foo").matches());
      assertTrue(p.matcher("foo.bar").matches());
   }

   public void testNoRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("foo", false);
      assertTrue(p.matcher("foo").matches());
      assertFalse(p.matcher("bar").matches());
      assertFalse(p.matcher("").matches());
      assertFalse(p.matcher("foo.bar").matches());
      assertFalse(p.matcher("foo.bar.juu").matches());
   }

   public void testRecursePackageMatcher()
   {
      Pattern p = Tools.getPackageMatcher("foo", true);
      assertTrue(p.matcher("foo").matches());
      assertTrue(p.matcher("foo.bar").matches());
      assertTrue(p.matcher("foo.bar.juu").matches());
      assertFalse(p.matcher("").matches());
      assertFalse(p.matcher("bar").matches());
      assertFalse(p.matcher("foobar").matches());
   }

   public void testUnquote()
   {
      assertEquals("", Tools.unquote(""));
      assertEquals("'", Tools.unquote("'"));
      assertEquals("\"", Tools.unquote("\""));
      assertEquals("'\"", Tools.unquote("'\""));
      assertEquals("\"'", Tools.unquote("\"'"));
      assertEquals("", Tools.unquote("''"));
      assertEquals("", Tools.unquote("\"\""));
      assertEquals("a", Tools.unquote("'a'"));
      assertEquals("a", Tools.unquote("\"a\""));
      assertEquals("'a\"", Tools.unquote("'a\""));
      assertEquals("\"a'", Tools.unquote("\"a'"));
   }
}
