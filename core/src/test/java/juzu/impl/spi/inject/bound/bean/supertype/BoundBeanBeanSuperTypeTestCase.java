package juzu.impl.spi.inject.bound.bean.supertype;

import org.junit.Test;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanBeanSuperTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public BoundBeanBeanSuperTypeTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      Apple apple = new Apple();
      bootstrap.bindBean(Fruit.class, null, apple);
      bootstrap.declareBean(Injected.class, null, null, null);
      boot();

      //
      Injected beanObject = getBean(Injected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
      assertSame(apple, beanObject.fruit);
   }
}
