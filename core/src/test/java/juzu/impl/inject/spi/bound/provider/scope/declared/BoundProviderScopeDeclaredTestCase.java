package juzu.impl.inject.spi.bound.provider.scope.declared;

import juzu.Scope;
import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.inject.spi.ScopedKey;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundProviderScopeDeclaredTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public BoundProviderScopeDeclaredTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    BeanProvider provider = new BeanProvider();

    //
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.bindProvider(Bean.class, Scope.REQUEST, null, provider);
    boot(Scope.REQUEST);

    //
    beginScoping();
    try {
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
      assertSame(scoped, provider.bean);
    }
    finally {
      endScoping();
    }
  }
}
