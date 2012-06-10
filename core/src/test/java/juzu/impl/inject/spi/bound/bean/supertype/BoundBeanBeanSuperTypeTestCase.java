package juzu.impl.inject.spi.bound.bean.supertype;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanBeanSuperTypeTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public BoundBeanBeanSuperTypeTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    Apple apple = new Apple();
    bootstrap.bindBean(Fruit.class, null, apple);
    bootstrap.declareBean(Injected.class, null, null, null);
    boot();

    //
    Injected beanObject = getBean(Injected.class);
    assertNotNull(beanObject);
    assertNotNull(beanObject.fruit);
    assertSame(apple, beanObject.fruit);
  }
}
