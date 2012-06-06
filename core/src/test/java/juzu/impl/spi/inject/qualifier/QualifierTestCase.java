package juzu.impl.spi.inject.qualifier;

import juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import juzu.impl.spi.inject.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class QualifierTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public QualifierTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, (Class<Injected>)null);
    bootstrap.declareBean(Bean.class, null, null, Bean.Red.class);
    bootstrap.declareBean(Bean.class, null, null, Bean.Green.class);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.getRed());
    assertEquals(Bean.Red.class, beanObject.getRed().getClass());
    assertNotNull(beanObject.getGreen());
    assertEquals(Bean.Green.class, beanObject.getGreen().getClass());
  }
}
