package juzu.impl.spi.inject.lifecycle.singleton;

import juzu.Scope;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleSingletonTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public LifeCycleSingletonTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, null);
    boot(Scope.SESSION);

    //
    Bean.construct = 0;
    Bean.destroy = 0;

    //
    beginScoping();
    try {
      B bean = mgr.resolveBean(Bean.class);
      I instance = mgr.create(bean);
      Bean o = (Bean)mgr.get(bean, instance);
      assertNotNull(o);
      assertEquals(1, Bean.construct);
      assertEquals(0, Bean.destroy);
      mgr.release(bean, instance);
      assertEquals(1, Bean.construct);
      assertEquals(0, Bean.destroy);
    }
    finally {
      endScoping();
    }
    assertEquals(1, Bean.construct);
    assertEquals(0, Bean.destroy);

    //
    mgr.shutdown();
    assertEquals(1, Bean.construct);
    assertEquals(1, Bean.destroy);
  }
}
