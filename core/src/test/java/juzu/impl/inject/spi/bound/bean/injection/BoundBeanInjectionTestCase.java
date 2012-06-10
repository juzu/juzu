package juzu.impl.inject.spi.bound.bean.injection;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanInjectionTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public BoundBeanInjectionTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    Bean singleton = new Bean();
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.bindBean(Bean.class, null, singleton);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.dependency);
    assertSame(singleton, injected.dependency);
  }
}
