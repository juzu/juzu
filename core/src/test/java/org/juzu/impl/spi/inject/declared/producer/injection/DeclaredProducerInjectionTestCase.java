package org.juzu.impl.spi.inject.declared.producer.injection;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredProducerInjectionTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public DeclaredProducerInjectionTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "producer", "injection");
      bootstrap.declareProvider(Bean.class, null, BeanProducer.class);
      bootstrap.declareBean(Injected.class, null, null);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
   }
}
