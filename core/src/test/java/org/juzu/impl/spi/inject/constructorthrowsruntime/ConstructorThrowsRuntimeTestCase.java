package org.juzu.impl.spi.inject.constructorthrowsruntime;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConstructorThrowsRuntimeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ConstructorThrowsRuntimeTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowsruntime");
      bootstrap.declareBean(Bean.class, null, null);
      boot();

      //
      try
      {
         getBean(Bean.class);
         throw failure("Was expecting exception");
      }
      catch (InvocationTargetException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }
}
