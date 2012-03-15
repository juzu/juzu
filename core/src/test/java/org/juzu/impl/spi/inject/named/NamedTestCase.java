package org.juzu.impl.spi.inject.named;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NamedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public NamedTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Injected.class, null, null);
      bootstrap.declareBean(Bean.class, null, Bean.Foo.class);
      bootstrap.declareBean(Bean.class, null, Bean.Bar.class);
      boot();

      //
      Injected beanObject = getBean(Injected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.getFoo());
      assertEquals(Bean.Foo.class, beanObject.getFoo().getClass());
      assertNotNull(beanObject.getBar());
      assertEquals(Bean.Bar.class, beanObject.getBar().getClass());

      //
      Object foo = getBean("foo");
      assertNotNull(foo);

      //
      assertNull(mgr.resolveBean("juu"));
   }
}
