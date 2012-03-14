package org.juzu.impl.spi.inject.bound.bean.supertype;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

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
      init("org", "juzu", "impl", "spi", "inject", "bound", "bean", "supertype");
      Apple apple = new Apple();
      bootstrap.bindBean(Fruit.class, null, apple);
      bootstrap.declareBean(Injected.class, null, null);
      boot();

      //
      Injected beanObject = getBean(Injected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
      assertSame(apple, beanObject.fruit);
   }
}
