package juzu.impl.spi.inject.siblingproducers;

import org.junit.Test;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SiblingProducersTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public SiblingProducersTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      bootstrap.declareProvider(Bean1.class, null, null, Bean1Provider.class);
      bootstrap.declareProvider(Bean2.class, null, null, Bean2Provider.class);
      bootstrap.addFileSystem(fs);
      boot();

      //
      Bean1 productExt1 = getBean(Bean1.class);
      assertNotNull(productExt1);

      //
      Bean2 productExt2 = getBean(Bean2.class);
      assertNotNull(productExt2);

      //
      Injected productInjected = getBean(Injected.class);
      assertNotNull(productInjected);
      assertNotNull(productInjected.productExt1);
      assertNotNull(productInjected.productExt2);
   }
}
