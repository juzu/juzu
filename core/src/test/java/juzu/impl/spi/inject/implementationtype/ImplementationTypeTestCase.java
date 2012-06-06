package juzu.impl.spi.inject.implementationtype;

import org.junit.Test;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ImplementationTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ImplementationTypeTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(AbstractBean.class, null, null, Bean.class);
      boot();

      //
      AbstractBean extended = getBean(AbstractBean.class);
      assertEquals(Bean.class, extended.getClass());
   }
}
