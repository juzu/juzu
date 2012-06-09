package juzu.impl.spi.inject.bound.bean.injected.install;

import juzu.Scope;
import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InstallInjectedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public InstallInjectedTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();

    bootstrap.bindBean(Injected.class, null, new Injected());
    bootstrap.declareBean(Bean.class, Scope.SINGLETON, null, null);
    boot();

    //
    Injected injected = getBean(Injected.class);
    Bean bean = getBean(Bean.class);
    assertNotNull(injected.bean);
    assertSame(bean, injected.bean);
  }
}
