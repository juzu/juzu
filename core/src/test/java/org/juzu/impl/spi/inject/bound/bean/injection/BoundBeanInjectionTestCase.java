package org.juzu.impl.spi.inject.bound.bean.injection;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanInjectionTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public BoundBeanInjectionTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      Bean singleton = new Bean();
      init();
      bootstrap.declareBean(Injected.class, null, null);
      bootstrap.bindBean(Bean.class, null, singleton);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
      assertSame(singleton, injected.dependency);
   }
}
