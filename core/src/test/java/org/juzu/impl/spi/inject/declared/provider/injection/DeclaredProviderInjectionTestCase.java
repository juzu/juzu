package org.juzu.impl.spi.inject.declared.provider.injection;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredProviderInjectionTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public DeclaredProviderInjectionTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "provider", "injection");
      bootstrap.declareProvider(Bean.class, null, BeanProvider.class);
      bootstrap.declareBean(Injected.class, null, null);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
   }

   @Test
   public void testProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "provider");
      bootstrap.bindProvider(Bean.class, null, new BeanProvider());
      boot();

      //
      Bean product = getBean(Bean.class);
      assertNotNull(product);
   }
}
