package juzu.impl.inject.spi.scope.singleton;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeSingletonTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public ScopeSingletonTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, null);
    boot();

    //
    Bean singleton1 = getBean(Bean.class);
    Bean singleton2 = getBean(Bean.class);
    assertSame(singleton1, singleton2);
  }
}
