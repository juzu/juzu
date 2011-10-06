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
   protected final long waitForOneMillis()
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
}
