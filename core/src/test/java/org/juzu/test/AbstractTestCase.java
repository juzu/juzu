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

package org.juzu.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTestCase extends TestCase
{

   /**
    * Wait for at least one millisecond, based on the current time clock.
    *
    * @return the time captured after the wait
    */
   public static long waitForOneMillis()
   {
      long snapshot = System.currentTimeMillis();
      while (true)
      {
         try
         {
            long now = System.currentTimeMillis();
            if (snapshot < now)
            {
               return now;
            }
            else
            {
               snapshot = now;
               Thread.sleep(1);
            }
         }
         catch (InterruptedException e)
         {
            AssertionFailedError afe = new AssertionFailedError("Was not expecting interruption");
            afe.initCause(e);
            throw afe;
         }
      }
   }

   public static void fail(Throwable t)
   {
      throw failure(t);
   }

   public static AssertionFailedError failure(Throwable t)
   {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(t);
      return afe;
   }

   public static AssertionFailedError failure(String msg)
   {
      return new AssertionFailedError(msg);
   }
}
