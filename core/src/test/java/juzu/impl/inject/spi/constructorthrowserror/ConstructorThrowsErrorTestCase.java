package juzu.impl.inject.spi.constructorthrowserror;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConstructorThrowsErrorTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public ConstructorThrowsErrorTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Bean.class, null, null, null);
    boot();

    //
    try {
      getBean(Bean.class);
      throw failure("Was expecting exception");
    }
    catch (InvocationTargetException e) {
      assertInstanceOf(UnknownError.class, e.getCause());
    }
  }
}
