package juzu.impl.spi.inject.declared.provider.injection;

import org.junit.Test;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;

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
      init();
      bootstrap.declareProvider(Bean.class, null, null, BeanProvider.class);
      bootstrap.declareBean(Injected.class, null, null, null);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
   }

   @Test
   public void testProvider() throws Exception
   {
      init();
      bootstrap.bindProvider(Bean.class, null, null, new BeanProvider());
      boot();

      //
      Bean product = getBean(Bean.class);
      assertNotNull(product);
   }
}
