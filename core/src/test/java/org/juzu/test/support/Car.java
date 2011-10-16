package org.juzu.test.support;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Car
{
   public long getIdentityHashCode()
   {
      return System.identityHashCode(this);
   }
}
