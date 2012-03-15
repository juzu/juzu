package org.juzu.impl.spi.inject.constructorthrowschecked;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;

import javax.naming.AuthenticationException;
import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConstructorThrowsCheckedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ConstructorThrowsCheckedTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
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
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }
}
