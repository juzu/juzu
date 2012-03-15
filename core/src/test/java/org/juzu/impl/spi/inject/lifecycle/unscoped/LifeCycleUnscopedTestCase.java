package org.juzu.impl.spi.inject.lifecycle.unscoped;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleUnscopedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public LifeCycleUnscopedTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Bean.class, null, null);
      boot();

      //
      Bean.construct = 0;
      Bean.destroy = 0;

      //
      beginScoping();
      try
      {
         B bean = mgr.resolveBean(Bean.class);
         I instance = mgr.create(bean);
         Bean o = (Bean)mgr.get(bean, instance);
         assertEquals(1, Bean.construct);
         assertEquals(0, Bean.destroy);
         mgr.release(bean, instance);
         assertEquals(1, Bean.construct);
         assertEquals(1, Bean.destroy);
      }
      finally
      {
         endScoping();
      }
      assertEquals(1, Bean.construct);
      assertEquals(1, Bean.destroy);
   }
}
