package juzu.impl.inject.spi.managerinjection;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InjectManagerTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public InjectManagerTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected managerInjected = getBean(Injected.class);
    assertNotNull(managerInjected);
    assertNotNull(managerInjected.manager);
    assertSame(mgr, managerInjected.manager);
  }
}
