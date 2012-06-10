package juzu.impl.inject.spi.declared.provider.injection;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredProviderInjectionTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public DeclaredProviderInjectionTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareProvider(Bean.class, null, null, BeanProvider.class);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.dependency);
  }

  @Test
  public void testProvider() throws Exception {
    init();
    bootstrap.bindProvider(Bean.class, null, null, new BeanProvider());
    boot();

    //
    Bean product = getBean(Bean.class);
    assertNotNull(product);
  }
}
