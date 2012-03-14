package org.juzu.impl.spi.inject.constructorthrowserror;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConstructorThrowsErrorTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ConstructorThrowsErrorTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowserror");
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
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }
}
