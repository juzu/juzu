package juzu.impl.spi.inject.declared.provider.injected;

import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProviderInjectedTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public ProviderInjectedTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    Dependency dependency = new Dependency();
    bootstrap.bindBean(Dependency.class, null, dependency);
    bootstrap.declareProvider(Bean.class, null, null, DependencyProvider.class);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.product);
    assertSame(dependency, injected.product.dependency);
  }
}
