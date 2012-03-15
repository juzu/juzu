package org.juzu.impl.spi.inject.scope.scoped;

import org.junit.Test;
import org.juzu.Scope;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.spi.inject.ScopedKey;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeScopedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ScopeScopedTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      bootstrap.declareBean(Bean.class, null, null, null);
      boot(Scope.REQUEST);

      //
      beginScoping();
      try
      {
         assertEquals(0, scopingContext.getEntries().size());
         Injected injected = getBean(Injected.class);
         assertNotNull(injected);
         assertNotNull(injected.scoped);
         String value = injected.scoped.getValue();
         assertEquals(1, scopingContext.getEntries().size());
         ScopedKey key = scopingContext.getEntries().keySet().iterator().next();
         assertEquals(Scope.REQUEST, key.getScope());
         Bean scoped = (Bean)scopingContext.getEntries().get(key).get();
         assertEquals(scoped.getValue(), value);
      }
      finally
      {
         endScoping();
      }
   }
}
