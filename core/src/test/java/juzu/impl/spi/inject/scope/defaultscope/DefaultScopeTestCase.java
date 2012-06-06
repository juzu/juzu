package juzu.impl.spi.inject.scope.defaultscope;

import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DefaultScopeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public DefaultScopeTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, null);
    boot();

    //
    Bean bean1 = getBean(Bean.class);
    Bean bean2 = getBean(Bean.class);
    assertTrue(bean1.count != bean2.count);
  }
}
