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
public class QNTestCase extends TestCase
{

   public void testIAE()
   {
      assertIAE(".");
      assertIAE(".a");
      assertIAE("a.");
      assertIAE("a..b");
      assertIAE("ab..c");
   }

   private void assertIAE(String value)
   {
      try
      {
         new QN(value);
         fail("Was expecting " + value + " to fail");
      }
      catch (IllegalArgumentException ignore)
      {
      }
   }

   public void testValues()
   {
      assertQN("");
      assertQN("a");
      assertQN("a");
      assertQN("a.b");
      assertQN("a.b.c");
   }

   private void assertQN(String value)
   {
      QN qn = new QN(value);
      assertEquals(value, qn.getValue());
   }

   public void testAppend()
   {
      assertAppend("a", "", "a");
      assertAppend("a.b", "a", "b");
      try
      {
         new QN("a").append("");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new QN("a").append("a.b");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new QN("a").append(null);
         fail();
      }
      catch (NullPointerException e)
      {
      }
   }

   private void assertAppend(String expected, String qn, String simpleName)
   {
      assertEquals(expected, new QN(qn).append(simpleName).getValue());
   }
}
