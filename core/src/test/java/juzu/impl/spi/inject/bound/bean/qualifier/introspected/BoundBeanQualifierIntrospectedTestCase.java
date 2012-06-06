package juzu.impl.spi.inject.bound.bean.qualifier.introspected;

import org.junit.Test;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanQualifierIntrospectedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public BoundBeanQualifierIntrospectedTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      Bean singleton = new Bean();
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      bootstrap.bindBean(Bean.class, null, singleton);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }
}
