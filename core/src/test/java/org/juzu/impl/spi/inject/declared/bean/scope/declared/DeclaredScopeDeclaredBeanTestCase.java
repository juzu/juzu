package org.juzu.impl.spi.inject.declared.bean.scope.declared;

import org.junit.Test;
import org.juzu.Scope;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.spi.inject.ScopedKey;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredScopeDeclaredBeanTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public DeclaredScopeDeclaredBeanTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      bootstrap.declareBean(Bean.class, Scope.REQUEST, null, null);
      boot(Scope.REQUEST);

      //
      beginScoping();
      try
      {
         assertEquals(0, scopingContext.getEntries().size());
         Injected injected = getBean(Injected.class);
         assertNotNull(injected);
         assertNotNull(injected.injected);
         String value = injected.injected.getValue();
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
