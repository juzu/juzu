package juzu.impl.inject.spi.supertype;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SuperTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public SuperTypeTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testSuperType() throws Exception {
    init();
    bootstrap.declareBean(Apple.class, null, null, null);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.fruit);
  }
}
