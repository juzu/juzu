package org.juzu.impl.spi.inject.supertype;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SuperTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public SuperTypeTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testSuperType() throws Exception
   {
      init();
      bootstrap.declareBean(Apple.class, null, null);
      bootstrap.declareBean(Injected.class, null, null);
      boot();

      //
      Injected beanObject = getBean(Injected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
   }
}
