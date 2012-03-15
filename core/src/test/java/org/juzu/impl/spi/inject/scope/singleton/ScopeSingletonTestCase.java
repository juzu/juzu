package org.juzu.impl.spi.inject.scope.singleton;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeSingletonTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ScopeSingletonTestCase(InjectImplementation di)
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
      Bean singleton1 = getBean(Bean.class);
      Bean singleton2 = getBean(Bean.class);
      assertSame(singleton1, singleton2);
   }
}
